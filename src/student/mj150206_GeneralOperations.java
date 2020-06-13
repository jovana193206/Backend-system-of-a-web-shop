package student;

import operations.GeneralOperations;

import java.sql.*;
import java.util.Calendar;
import java.util.StringTokenizer;

public class mj150206_GeneralOperations implements GeneralOperations {

    private Calendar currentTime;

    public mj150206_GeneralOperations() {
        currentTime = null;
    }

    @Override
    public void setInitialTime(Calendar time) {
        currentTime = Calendar.getInstance();
        currentTime.clear();
        currentTime.setTimeInMillis(time.getTimeInMillis());
    }

    @Override
    public Calendar time(int days) {
        currentTime.add(Calendar.DAY_OF_MONTH, days);

        Connection connection = DB.getInstance().getConnection();
        String getTransport = "select idTransport, idOrder, idItem, CurrentLine, DistanceLeft from Transport ";
        try(Statement trSt = connection.createStatement();
            ResultSet trRs = trSt.executeQuery(getTransport);) {
            connection.setAutoCommit(false);
            //Obradjujemo red po red tabele Transport gde je sto znaci da je potrebna promena linije
            while(trRs.next()) {
                int daysLeft = days;  //Govori koliko vremena jos treba pomeriti
                int idTransport = trRs.getInt(1);
                int idOrder = trRs.getInt(2);
                Integer idItem = trRs.getInt(3);  //moze biti null
                int curLine = trRs.getInt(4);
                int distLeft = trRs.getInt(5);
                if(distLeft == 0) continue; //ovo je red za Item koji je stigao u assemblyCity i samo ceka ostale
                String path = null;
                if(idItem == null || idItem == 0) {
                    try(Statement getPathO = connection.createStatement();
                        ResultSet pathORs = getPathO.executeQuery("select Path from [Order] where idOrder = " + idOrder + " ")) {
                        if(pathORs.next()) path = pathORs.getString(1);
                    }
                }
                else {
                    try(Statement getPathI = connection.createStatement();
                        ResultSet pathIRs = getPathI.executeQuery("select Path from Item where idItem = " + idItem + " ")) {
                        if(pathIRs.next()) path = pathIRs.getString(1);
                    }
                }
                while(daysLeft > 0) {
                    if(distLeft > daysLeft) {
                        String updateDist = "update Transport set DistanceLeft = DistanceLeft - " + daysLeft + " where idTransport = " + idTransport + " ";
                        try(Statement upDistSt = connection.createStatement()) {
                            int updatedDist = upDistSt.executeUpdate(updateDist);
                            break; //zavrsili smo sa ovim redom tabele Transport
                        }
                    }
                    daysLeft -= distLeft;
                    //Nalazimo sledecu liniju iz Path-a za ovaj Item/Order
                    int cityFrom = 0, cityTo = 0;
                    String getCityFrom = "select CityTo from Line where idLine = " + curLine + " ";
                    try(Statement cityFromSt = connection.createStatement();
                        ResultSet cityFromRs = cityFromSt.executeQuery(getCityFrom)) {
                        if(cityFromRs.next()) cityFrom = cityFromRs.getInt(1);
                    }
                    StringTokenizer pathTok = new StringTokenizer(path, ",");
                    int token = Integer.parseInt(pathTok.nextToken());
                    while(pathTok.hasMoreTokens() && token != cityFrom) token = Integer.parseInt(pathTok.nextToken());
                    if(pathTok.hasMoreTokens()) {
                        //Nismo stigli do odredista, azuriramo liniju i idemo u sledecu iteraciju petlje
                        cityTo = Integer.parseInt(pathTok.nextToken());
                        String getNextLine = "select idLine, Distance from Line where CityFrom = " + cityFrom + " and CityTo = " + cityTo + " ";
                        int nextLineId = 0, nextDist = 0;
                        try(Statement nextLineSt = connection.createStatement();
                            ResultSet nextLineRs = nextLineSt.executeQuery(getNextLine)) {
                            if(nextLineRs.next()) {
                                nextLineId = nextLineRs.getInt(1);
                                nextDist = nextLineRs.getInt(2);
                            }
                        }
                        curLine = nextLineId;
                        distLeft = nextDist;
                        String updateTransportStr = "update Transport set CurrentLine = ?, DistanceLeft = ? where idTransport = ? ";
                        try(PreparedStatement upTrPs = connection.prepareStatement(updateTransportStr)) {
                            upTrPs.setInt(1, nextLineId);
                            upTrPs.setInt(2, nextDist);
                            upTrPs.setInt(3, idTransport);
                            int updatedTr = upTrPs.executeUpdate();
                            int x = 2;
                        }
                    }
                    else {
                        //Stigli smo do odredista
                        if(idItem == null || idItem == 0) {
                            //Order
                            long sub = daysLeft*24*60*60*1000; //convert daysLeft into millis
                            Timestamp receivedTime = new Timestamp(currentTime.getTimeInMillis() - sub);
                            String updateOrder = "update [Order] set ReceivedTime = ?, [State] = 'arrived' where idOrder = ? ";
                            String deleteORow = "delete from Transport where idTransport = " + idTransport;
                            try(PreparedStatement upOrdPs = connection.prepareStatement(updateOrder);
                                Statement delORowSt = connection.createStatement()) {
                                upOrdPs.setTimestamp(1, receivedTime);
                                upOrdPs.setInt(2, idOrder);
                                upOrdPs.executeUpdate();
                                int deletedORows = delORowSt.executeUpdate(deleteORow);
                            }
                            break; //Zavrsili smo sa ovim redom tabele Transport, taj red vise i ne postoji u toj tabeli, obrisan je
                        }
                        else {
                            //Item
                            String arrivedStr = "update Transport set DistanceLeft = 0 where idTransport = " + idTransport + " ";
                            try(Statement arrivedSt = connection.createStatement()) {
                                int arrived = arrivedSt.executeUpdate(arrivedStr);
                            }
                            String proveraStr = "select idTransport from Transport where idOrder = " + idOrder + " and DistanceLeft > 0 ";
                            try(Statement proveraSt = connection.createStatement();
                                ResultSet proveraRs = proveraSt.executeQuery(proveraStr)) {
                                if(proveraRs.next()) {
                                    //Nisu svi delovi porudzbine stigli u grad sklapanja
                                    break;  //Zavrsili smo sa ovim redom tabele Transport
                                }
                                else {
                                    //Svi delovi porudzbine su stigli u grad sklapanja
                                    String deleteItemsStr = "delete from Transport where idOrder = " + idOrder + " ";
                                    try(Statement delItSt = connection.createStatement()) {
                                        int deletedItems = delItSt.executeUpdate(deleteItemsStr);
                                    }
                                    //Ubaci red u Transport tabelu za celu porudzbinu idOrder
                                    try(Statement getPathO = connection.createStatement();
                                        ResultSet pathORs = getPathO.executeQuery("select Path from [Order] where idOrder = " + idOrder + " ")) {
                                        if(pathORs.next()) path = pathORs.getString(1);
                                    }
                                    StringTokenizer pathTokenizer = new StringTokenizer(path, ",");
                                    if(pathTokenizer.countTokens() < 1) break;
                                    if(pathTokenizer.countTokens() == 1) {
                                        //Order assemblyCity = buyerCity => nije potreban transport za Order, vec je arrived
                                        Timestamp receivedTime = new Timestamp(currentTime.getTimeInMillis());
                                        String updateOrder = "update [Order] set ReceivedTime = ?, [State] = 'arrived' where idOrder = ? ";
                                        try(PreparedStatement upOrdPs = connection.prepareStatement(updateOrder)) {
                                            upOrdPs.setTimestamp(1, receivedTime);
                                            upOrdPs.setInt(2, idOrder);
                                            upOrdPs.executeUpdate();
                                        }
                                        break; //Zavrsili smo sa ovim redom tabele Transport
                                    }
                                    cityFrom = Integer.parseInt(pathTokenizer.nextToken());
                                    cityTo = Integer.parseInt(pathTokenizer.nextToken());
                                    String getNextLine = "select idLine, Distance from Line where CityFrom = " + cityFrom + " and CityTo = " + cityTo + " ";
                                    idItem = null;
                                    String insertTransport = "insert into Transport(idOrder, CurrentLine, DistanceLeft) values (?,?,?) ";
                                    try(Statement lineSt = connection.createStatement();
                                        ResultSet lineRs = lineSt.executeQuery(getNextLine);
                                        PreparedStatement insTrPs = connection.prepareStatement(insertTransport, PreparedStatement.RETURN_GENERATED_KEYS)) {
                                        if(!lineRs.next()) break;
                                        curLine = lineRs.getInt(1);
                                        distLeft = lineRs.getInt(2);
                                        insTrPs.setInt(1, idOrder);
                                        insTrPs.setInt(2, curLine);
                                        insTrPs.setInt(3, distLeft);
                                        insTrPs.executeUpdate();
                                        try(ResultSet generatedKey = insTrPs.getGeneratedKeys()) {
                                            if(generatedKey.next()) idTransport = generatedKey.getInt(1);
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        return currentTime;
    }

    @Override
    public Calendar getCurrentTime() {
        return currentTime;
    }

    @Override
    public void eraseAll() {
        Connection connection = DB.getInstance().getConnection();
        String deleteAll = "EXEC sp_MSForEachTable 'ALTER TABLE ? NOCHECK CONSTRAINT ALL' \n" +
                            "EXEC sp_MSForEachTable 'ALTER TABLE ? DISABLE TRIGGER ALL' \n" +
                            "EXEC sp_MSForEachTable 'DELETE FROM ?' \n" +
                            "DBCC CHECKIDENT('[Buyer]', RESEED, 0) \n" +
                            "DBCC CHECKIDENT('[City]', RESEED, 0) \n" +
                            "DBCC CHECKIDENT('[Line]', RESEED, 0) \n" +
                            "DBCC CHECKIDENT('[Shop]', RESEED, 0) \n" +
                            "DBCC CHECKIDENT('[Article]', RESEED, 0) \n" +
                            "DBCC CHECKIDENT('[Order]', RESEED, 0) \n" +
                            "DBCC CHECKIDENT('[Transaction]', RESEED, 0) \n" +
                            "DBCC CHECKIDENT('[Transport]', RESEED, 0) \n" +
                            "DBCC CHECKIDENT('[Item]', RESEED, 0) \n" +
                            "EXEC sp_MSForEachTable 'ALTER TABLE ? CHECK CONSTRAINT ALL' \n" +
                            "EXEC sp_MSForEachTable 'ALTER TABLE ? ENABLE TRIGGER ALL' \n";
        try (Statement st = connection.createStatement()) {
            st.executeUpdate(deleteAll);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
