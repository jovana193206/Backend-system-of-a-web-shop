CREATE PROCEDURE dbo.SP_FINAL_PRICE 
    @idOrder int,
    @finalPrice decimal(10,3) OUTPUT 
AS
    declare @nomPrice decimal(10,3)
	set @nomPrice = dbo.nominalPrice(@idOrder)
	declare @discount decimal(10,3)
	set @discount = dbo.discountSum(@idOrder, @nomPrice)
	set @finalPrice = @nomPrice - @discount 
	 
RETURN 0