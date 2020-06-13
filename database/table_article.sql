USE [mj150206]
GO

/****** Object:  Table [dbo].[Article]    Script Date: 6/13/2020 9:58:20 PM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[Article](
	[idArticle] [dbo].[id] IDENTITY(1,1) NOT NULL,
	[Price] [dbo].[double] NULL,
	[Count] [int] NULL,
	[idShop] [dbo].[id] NOT NULL,
	[Name] [dbo].[TextField] NULL,
 CONSTRAINT [XPKArticle] PRIMARY KEY CLUSTERED 
(
	[idArticle] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[Article] ADD  CONSTRAINT [Article_Count_Default_1666263627]  DEFAULT ((0)) FOR [Count]
GO

ALTER TABLE [dbo].[Article]  WITH NOCHECK ADD  CONSTRAINT [R_3] FOREIGN KEY([idShop])
REFERENCES [dbo].[Shop] ([idShop])
ON UPDATE CASCADE
GO

ALTER TABLE [dbo].[Article] CHECK CONSTRAINT [R_3]
GO


