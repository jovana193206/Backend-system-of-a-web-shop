USE [mj150206]
GO

/****** Object:  Table [dbo].[Transaction]    Script Date: 6/13/2020 10:03:37 PM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[Transaction](
	[idTransaction] [dbo].[id] IDENTITY(1,1) NOT NULL,
	[ExecutionTime] [datetime] NULL,
	[Amount] [dbo].[double] NULL,
	[idOrder] [dbo].[id] NOT NULL,
	[idShop] [dbo].[id] NULL,
 CONSTRAINT [XPKTransaction] PRIMARY KEY CLUSTERED 
(
	[idTransaction] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[Transaction]  WITH NOCHECK ADD  CONSTRAINT [R_13] FOREIGN KEY([idOrder])
REFERENCES [dbo].[Order] ([idOrder])
ON UPDATE CASCADE
GO

ALTER TABLE [dbo].[Transaction] CHECK CONSTRAINT [R_13]
GO

ALTER TABLE [dbo].[Transaction]  WITH NOCHECK ADD  CONSTRAINT [R_15] FOREIGN KEY([idShop])
REFERENCES [dbo].[Shop] ([idShop])
ON UPDATE CASCADE
GO

ALTER TABLE [dbo].[Transaction] CHECK CONSTRAINT [R_15]
GO


