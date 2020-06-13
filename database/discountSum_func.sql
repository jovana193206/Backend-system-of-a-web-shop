USE [mj150206]
GO

/****** Object:  UserDefinedFunction [dbo].[discountSum]    Script Date: 7/6/2019 4:48:00 AM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO



alter FUNCTION [dbo].[discountSum]
(
    @idOrder int,
	@nomPrice decimal(10,3)
)
RETURNS DECIMAL(10,3)
AS
BEGIN
    declare @orderCnt int
	set @orderCnt = 0
	declare @idBuyer int
	declare @items cursor
	declare @idItem int
	declare @idArticle int
	declare @articleCount int
	declare @articlePrice decimal(10,3)
	declare @idShop int
	declare @shopDiscount int
	declare @discountSum decimal(10,3)

	set @discountSum = 0

	set @items = cursor for
	select idItem, idArticle, [Count]
	from Item
	where idOrder = @idOrder

	open @items

	fetch from @items
	into @idItem, @idArticle, @articleCount

	while(@@FETCH_STATUS = 0)
	begin
		select @articlePrice = Price, @idShop = idShop from Article where idArticle = @idArticle
		select @shopDiscount = Discount from Shop where idShop = @idShop
		set @discountSum = @discountSum + @articleCount * @articlePrice * @shopDiscount / 100
		fetch from @items
		into @idItem, @idArticle, @articleCount
	end

	close @items
	deallocate @items

	select @idBuyer = idBuyer from [Order] where idOrder = @idOrder

	select @orderCnt = count(T.idTransaction) 
	from [Transaction] T, [Order] O
	where T.idOrder = O.idOrder and O.idBuyer = @idBuyer and T.idShop is NULL
		and T.Amount > 10000 and datediff(day, T.ExecutionTime, getdate()) between 0 and 30

	if(@orderCnt > 0) set @discountSum = @discountSum + (@nomPrice - @discountSum) * 2 /100

	return @discountSum

END

GO


