USE [mj150206]
GO

/****** Object:  Table [dbo].[Transport]    Script Date: 6/13/2020 10:04:15 PM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[Transport](
	[idTransport] [dbo].[id] IDENTITY(1,1) NOT NULL,
	[idOrder] [dbo].[id] NOT NULL,
	[idItem] [dbo].[id] NULL,
	[CurrentLine] [dbo].[id] NOT NULL,
	[DistanceLeft] [int] NULL,
 CONSTRAINT [XPKTransport] PRIMARY KEY CLUSTERED 
(
	[idTransport] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[Transport]  WITH NOCHECK ADD  CONSTRAINT [R_16] FOREIGN KEY([idOrder])
REFERENCES [dbo].[Order] ([idOrder])
ON UPDATE CASCADE
GO

ALTER TABLE [dbo].[Transport] CHECK CONSTRAINT [R_16]
GO

ALTER TABLE [dbo].[Transport]  WITH NOCHECK ADD  CONSTRAINT [R_17] FOREIGN KEY([idItem])
REFERENCES [dbo].[Item] ([idItem])
GO

ALTER TABLE [dbo].[Transport] CHECK CONSTRAINT [R_17]
GO

ALTER TABLE [dbo].[Transport]  WITH NOCHECK ADD  CONSTRAINT [R_18] FOREIGN KEY([CurrentLine])
REFERENCES [dbo].[Line] ([idLine])
ON UPDATE CASCADE
GO

ALTER TABLE [dbo].[Transport] CHECK CONSTRAINT [R_18]
GO


