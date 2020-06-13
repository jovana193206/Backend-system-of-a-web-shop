USE [mj150206]
GO

/****** Object:  Trigger [dbo].[TR_TRANSFER_MONEY_TO_SHOPS]    Script Date: 7/7/2019 10:18:13 PM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO



CREATE TRIGGER [dbo].[TR_TRANSFER_MONEY_TO_SHOPS]
    ON [dbo].[Order]
    FOR UPDATE
    AS
    BEGIN 
		declare @kursorO cursor
		declare @idOrder int
		declare @newState varchar(100)
		declare @execTime datetime

		set @kursorO = cursor for
		select idOrder, State
		from inserted

		open @kursorO

		fetch from @kursorO
		into @idOrder, @newState

		while(@@FETCH_STATUS = 0)
		begin
			if(@newState = 'arrived')
			begin
				select @execTime = Receivedtime from [Order] where idOrder = @idOrder

				declare @shops cursor
				declare @idShop int

				set @shops = cursor for
				select distinct(A.idShop)
				from Item I, Article A
				where I.idOrder = @idOrder and I.idArticle = A.idArticle

				open @shops

				fetch from @shops 
				into @idShop

				while(@@FETCH_STATUS = 0)
				begin
					declare @amount decimal(10,3)
					select @amount = sum(I.[Count] * A.Price - I.[Count] * A.Price * S.Discount / 100)
					from Item I, Article A, Shop S
					where I.idOrder = @idOrder and I.idArticle = A.idArticle and A.idShop = S.idShop and S.idShop = @idShop

					set @amount = @amount * 95 / 100

					insert into [Transaction] (idOrder, idShop, Amount, ExecutionTime)
					values (@idOrder, @idShop, @amount, @execTime)

					fetch from @shops 
					into @idShop
				end

				close @shops
				deallocate @shops
			end

			fetch from @kursorO
			into @idOrder, @newState
		end

		close @kursorO
		deallocate @kursorO
    END
GO

ALTER TABLE [dbo].[Order] DISABLE TRIGGER [TR_TRANSFER_MONEY_TO_SHOPS]
GO


