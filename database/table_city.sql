USE [mj150206]
GO

/****** Object:  Table [dbo].[City]    Script Date: 6/13/2020 9:59:28 PM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[City](
	[idCity] [dbo].[id] IDENTITY(1,1) NOT NULL,
	[Name] [dbo].[TextField] NULL,
 CONSTRAINT [XPKCity] PRIMARY KEY CLUSTERED 
(
	[idCity] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [XAK1CityName] UNIQUE NONCLUSTERED 
(
	[Name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO


