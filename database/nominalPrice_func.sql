
CREATE FUNCTION [dbo].[nominalPrice]
(
    @idOrder int
)
RETURNS DECIMAL(10,3)
AS
BEGIN

    RETURN (
		select sum(A.Price * I.[Count]) 
		from Item I, Article A
		where I.idOrder = @idOrder and I.idArticle = A.idArticle
	)

END
