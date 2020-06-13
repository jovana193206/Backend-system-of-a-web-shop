USE [mj150206]
GO

/****** Object:  Table [dbo].[Order]    Script Date: 6/13/2020 10:00:56 PM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[Order](
	[idOrder] [dbo].[id] IDENTITY(1,1) NOT NULL,
	[idBuyer] [dbo].[id] NOT NULL,
	[State] [dbo].[TextField] NULL,
	[SentTime] [datetime] NULL,
	[ReceivedTime] [datetime] NULL,
	[AssemblyCity] [dbo].[id] NULL,
	[Path] [dbo].[TextField] NULL,
 CONSTRAINT [XPKOrder] PRIMARY KEY CLUSTERED 
(
	[idOrder] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[Order] ADD  CONSTRAINT [Order_State_Default_977936595]  DEFAULT ('created') FOR [State]
GO

ALTER TABLE [dbo].[Order]  WITH NOCHECK ADD  CONSTRAINT [R_10] FOREIGN KEY([idBuyer])
REFERENCES [dbo].[Buyer] ([idBuyer])
ON UPDATE CASCADE
GO

ALTER TABLE [dbo].[Order] CHECK CONSTRAINT [R_10]
GO

ALTER TABLE [dbo].[Order]  WITH NOCHECK ADD  CONSTRAINT [R_14] FOREIGN KEY([AssemblyCity])
REFERENCES [dbo].[City] ([idCity])
GO

ALTER TABLE [dbo].[Order] CHECK CONSTRAINT [R_14]
GO

ALTER TABLE [dbo].[Order]  WITH NOCHECK ADD  CONSTRAINT [Order_State_Validation_876029502] CHECK  (([State]='created' OR [State]='sent' OR [State]='arrived'))
GO

ALTER TABLE [dbo].[Order] CHECK CONSTRAINT [Order_State_Validation_876029502]
GO


