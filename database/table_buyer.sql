USE [mj150206]
GO

/****** Object:  Table [dbo].[Buyer]    Script Date: 6/13/2020 9:58:48 PM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[Buyer](
	[idBuyer] [dbo].[id] IDENTITY(1,1) NOT NULL,
	[Name] [dbo].[TextField] NULL,
	[Credit] [dbo].[double] NULL,
	[idCity] [dbo].[id] NOT NULL,
 CONSTRAINT [XPKBuyer] PRIMARY KEY CLUSTERED 
(
	[idBuyer] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[Buyer] ADD  CONSTRAINT [Buyer_Credit_Default_1509156218]  DEFAULT ((0)) FOR [Credit]
GO

ALTER TABLE [dbo].[Buyer]  WITH NOCHECK ADD  CONSTRAINT [R_1] FOREIGN KEY([idCity])
REFERENCES [dbo].[City] ([idCity])
ON UPDATE CASCADE
GO

ALTER TABLE [dbo].[Buyer] CHECK CONSTRAINT [R_1]
GO


