package student;

import jdk.nashorn.internal.codegen.CompilerConstants;
import operations.GeneralOperations;
import operations.OrderOperations;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

public class mj150206_OrderOperations implements OrderOperations {

    private GeneralOperations timeOp = null;

    public mj150206_OrderOperations(GeneralOperations timeOp) {
        this.timeOp = timeOp;
    }

    @Override
    public int addArticle(int orderId, int articleId, int count) {
        Connection connection = DB.getInstance().getConnection();
        String getArticleCount = "select Count from Article where idArticle = " + articleId + " ";
        String getItem = "select idItem from Item where idOrder = " + orderId + " and idArticle = " + articleId + " ";
        String insertItem = "insert into Item(idArticle, idOrder, Count) values (?, ?, ?) ";
        String updateItem = "update Item set Count = Count + ? where idItem = ? ";
        try (Statement stmt = connection.createStatement();
             ResultSet articleCount = stmt.executeQuery(getArticleCount)) {
            int newCountInShop = 0;
            //Check if there are enough articles in the store
            if(articleCount.next()) {
                if(articleCount.getInt(1) < count) return -1;
                else newCountInShop = articleCount.getInt(1) - count;
            }
            else return -1;
            connection.setAutoCommit(false);
            //Decrease the count of the article in the store
            String decreaseCountInShop = "update Article set Count = " + newCountInShop + " where idArticle = " + articleId + " ";
            int updated = stmt.executeUpdate(decreaseCountInShop);
            if(updated != 1) {
                connection.rollback();
                connection.setAutoCommit(true);
                return -1;
            }
            //Check if there already exists an Item with articleId and orderId
            try(ResultSet item = stmt.executeQuery(getItem)) {
                if(item.next()) {
                    //Update the Count of the existing item
                    try(PreparedStatement ps1 = connection.prepareStatement(updateItem)) {
                        ps1.setInt(1, count);
                        ps1.setInt(2, item.getInt(1));
                        updated = ps1.executeUpdate();
                        if(updated != 1) {
                            connection.rollback();
                            connection.setAutoCommit(true);
                            return -1;
                        }
                        else {
                            connection.commit();
                            connection.setAutoCommit(true);
                            return item.getInt(1);
                        }
                    }
                }
                else {
                    //Create a new Item
                    try(PreparedStatement ps2 = connection.prepareStatement(insertItem, PreparedStatement.RETURN_GENERATED_KEYS)) {
                        ps2.setInt(1, articleId);
                        ps2.setInt(2, orderId);
                        ps2.setInt(3, count);
                        updated = ps2.executeUpdate();
                        if(updated != 1) return -1;
                        try(ResultSet rs = ps2.getGeneratedKeys()) {
                            if(rs.next()) {
                                connection.commit();
                                connection.setAutoCommit(true);
                                return rs.getInt(1);
                            }
                        }
                    }
                }
            }
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
        return -1;
    }

    @Override
    public int removeArticle(int orderId, int articleId) {
        Connection connection = DB.getInstance().getConnection();
        String getCount = "select Count from Item where idOrder = " + orderId + " and idArticle = " + articleId + " ";
        String deleteItem = "delete from Item where idOrder = " + orderId + " and idArticle = " + articleId + " ";
        String updateArticle = "update Article set Count = Count + ? where idArticle = ? ";
        try(Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(getCount)) {
            connection.setAutoCommit(false);
            int cnt;
            if(rs.next()) {
                cnt = rs.getInt(1);
            }
            else  {
                connection.setAutoCommit(true);
                return -1;
            }
            int updated = st.executeUpdate(deleteItem);
            if(updated != 1) {
                connection.rollback();
                connection.setAutoCommit(true);
                return -1;
            }
            try(PreparedStatement ps = connection.prepareStatement(updateArticle)) {
                ps.setInt(1, cnt);
                ps.setInt(2, articleId);
                updated = ps.executeUpdate();
                if(updated != 1) {
                    connection.rollback();
                    connection.setAutoCommit(true);
                    return -1;
                }
                else {
                    connection.commit();
                    connection.setAutoCommit(true);
                    return 1;
                }
            }
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
        return -1;
    }

    @Override
    public List<Integer> getItems(int orderId) {
        Connection connection = DB.getInstance().getConnection();
        String query = "select idItem from Item where idOrder = " + orderId + " ";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            if(!rs.next()) return null;
            List<Integer> ids = new ArrayList<>();
            ids.add(rs.getInt(1));
            while(rs.next()) {
                ids.add(rs.getInt(1));
            }
            return ids;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int completeOrder(int orderId) {
        Connection connection = DB.getInstance().getConnection();
        String findAssemblyCity = "{call SP_ASSEMBLY_CITY (?,?)}";
        String getBuyerCity = "select b.idCity from Buyer b, [Order] o where o.idBuyer = b.idBuyer and o.idOrder = " + orderId + " ";
        try(CallableStatement cstmt = connection.prepareCall(findAssemblyCity);
            Statement st = connection.createStatement();
            ResultSet buyersCity = st.executeQuery(getBuyerCity)) {
            connection.setAutoCommit(false);
            if(!buyersCity.next()) {
                connection.rollback();
                connection.setAutoCommit(true);
                return -1;
            }
            int buyersCityId = buyersCity.getInt(1);
            cstmt.setInt(1, buyersCityId);
            cstmt.setInt(2, orderId);
            cstmt.execute();
            String getStartCities = "select distinct(s.idCity) from Shop s, Article a, Item i, [Order] o " +
                                    "where s.idShop = a.idShop and i.idArticle = a.idArticle and o.idOrder = i.idOrder and o.idOrder = " + orderId + " ";
            String getAssemblyCity = "select AssemblyCity from [Order] where idOrder = " + orderId;
            try(Statement startCitiesSt = connection.createStatement();
                ResultSet startCities = startCitiesSt.executeQuery(getStartCities);
                Statement assemblyCitySt = connection.createStatement();
                ResultSet assemblyCity = assemblyCitySt.executeQuery(getAssemblyCity);
                CallableStatement dijkstraCall = connection.prepareCall("{call SP_DIJKSTRA (?,?,?,?)}")) {
                if(!assemblyCity.next()) {
                    connection.rollback();
                    connection.setAutoCommit(true);
                    return -1;
                }
                int assemblyId = assemblyCity.getInt(1);
                while(startCities.next()) {
                    dijkstraCall.setInt(1, startCities.getInt((1)));
                    dijkstraCall.setInt(2, assemblyId);
                    dijkstraCall.setInt(3, orderId);
                    dijkstraCall.setInt(4, 0);
                    dijkstraCall.execute();
                }
                String insertTransportStr = "insert into Transport (idOrder, idItem, CurrentLine, DistanceLeft) values (?,?,?,?) ";
                try(Statement itemsSt = connection.createStatement();
                    ResultSet itemsRes = itemsSt.executeQuery("select idItem, Path from Item where idOrder = " + orderId);
                    PreparedStatement insertTransport = connection.prepareStatement(insertTransportStr)) {
                    int newItemId = 0;
                    String newItemPath = null;
                    while(itemsRes.next()) {
                        newItemId = itemsRes.getInt(1);
                        newItemPath = itemsRes.getString(2);
                        //Treba iz Path izvuci prva 2 id-ja gradova i naci odgovarajucu liniju
                        StringTokenizer pathTokenizer = new StringTokenizer(newItemPath, ",");
                        if(pathTokenizer.countTokens() < 1) {
                            connection.rollback();
                            connection.setAutoCommit(true);
                            return -1;
                        }
                        if(pathTokenizer.countTokens() == 1) {
                            //Item je vec u assemblyCity => nema potrebe za njegovim transportom, ne ubacujemo red u tabelu Transport
                            //prelazimo na sledeci item
                            continue;
                        }
                        int cityFrom = Integer.parseInt(pathTokenizer.nextToken());
                        int cityTo = Integer.parseInt(pathTokenizer.nextToken());
                        String findLine = "select idLine, Distance from Line where CityFrom = " + cityFrom + " and CityTo = " + cityTo + " ";
                        try(Statement lineSt = connection.createStatement();
                            ResultSet lineRs = lineSt.executeQuery(findLine)) {
                            if(!lineRs.next()) {
                                connection.rollback();
                                connection.setAutoCommit(true);
                                return -1;
                            }
                            insertTransport.setInt(1, orderId);
                            insertTransport.setInt(2, newItemId);
                            int currLineId = lineRs.getInt(1);
                            int distLeft = lineRs.getInt(2);
                            insertTransport.setInt(3, currLineId);
                            insertTransport.setInt(4, distLeft);
                            insertTransport.executeUpdate();
                        }
                    }
                    //Dijkstra za odredjivanje Path za Order
                    dijkstraCall.setInt(1, assemblyId);
                    dijkstraCall.setInt(2, buyersCityId);
                    dijkstraCall.setInt(3, orderId);
                    dijkstraCall.setInt(4, 1);
                    dijkstraCall.execute();
                    //Podesavanje SentTime i State za order
                    Timestamp sentTime = new Timestamp(timeOp.getCurrentTime().getTimeInMillis());
                    String updateOrder = "update [Order] set SentTime = ?, [State] = 'sent' where idOrder = ? ";
                    try(PreparedStatement updateOrderPs = connection.prepareStatement(updateOrder)) {
                        updateOrderPs.setTimestamp(1, sentTime);
                        updateOrderPs.setInt(2, orderId);
                        int updated = updateOrderPs.executeUpdate();
                        if(updated != 1) {
                            connection.rollback();
                            connection.setAutoCommit(true);
                            return -1;
                        }
                    }
                    String insertBuyersTransaction = "insert into [Transaction] (ExecutionTime, Amount, idOrder) values (?,?,?) ";
                    String call_SP_FINAL_PRICE = "{call SP_FINAL_PRICE (?,?)}";
                    try(CallableStatement callSt = connection.prepareCall(call_SP_FINAL_PRICE);
                        PreparedStatement insertTrPs = connection.prepareStatement(insertBuyersTransaction)) {
                        callSt.setInt(1,orderId);
                        callSt.registerOutParameter(2, Types.DECIMAL);
                        callSt.execute();
                        BigDecimal finalPrice = callSt.getBigDecimal(2);
                        insertTrPs.setTimestamp(1, new Timestamp(timeOp.getCurrentTime().getTimeInMillis()));
                        insertTrPs.setBigDecimal(2, finalPrice);
                        insertTrPs.setInt(3, orderId);
                        int insertedTrans = insertTrPs.executeUpdate();
                    }
                    connection.commit();
                    return 1;
                }
            }
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
        return -1;
    }

    @Override
    public BigDecimal getFinalPrice(int orderId) {
        Connection connection = DB.getInstance().getConnection();
        String procCall = "{call SP_FINAL_PRICE (?,?)}";
        String getState = "select [State] from [Order] where idOrder = " + orderId + " ";
        try(CallableStatement stmt = connection.prepareCall(procCall);
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(getState)) {
            if(rs.next()) {
                String stateStr = rs.getString(1);
                if(stateStr.equals("created")) return new BigDecimal(-1);
            }
            else return new BigDecimal(-1);
            stmt.setInt(1, orderId);
            stmt.registerOutParameter(2, Types.DECIMAL);
            stmt.execute();
            return stmt.getBigDecimal(2).setScale(3);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new BigDecimal(-1);
    }

    @Override
    public BigDecimal getDiscountSum(int orderId) {
        Connection connection = DB.getInstance().getConnection();
        String discountCall = "{? = call discountSum (?,?)}";
        String nominalCall = "{? = call nominalPrice (?)}";
        String getState = "select [State] from [Order] where idOrder = " + orderId + " ";
        try(CallableStatement callDisc = connection.prepareCall(discountCall);
            CallableStatement callNom = connection.prepareCall(nominalCall);
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(getState)) {
            if(rs.next()) {
                if(rs.getString(1).equals("created")) return new BigDecimal(-1);
            }
            else return new BigDecimal(-1);
            callNom.setInt(2, orderId);
            callNom.registerOutParameter(1, Types.DECIMAL);
            callNom.execute();
            BigDecimal nominal = callNom.getBigDecimal(1);
            callDisc.setInt(2, orderId);
            callDisc.setBigDecimal(3, nominal);
            callDisc.registerOutParameter(1, Types.DECIMAL);
            callDisc.execute();
            return callDisc.getBigDecimal(1).setScale(3);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new BigDecimal(-1);
    }

    @Override
    public String getState(int orderId) {
        Connection connection = DB.getInstance().getConnection();
        String getState = "select [State] from [Order] where idOrder = " + orderId + " ";
        try(Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(getState)) {
            if(rs.next()) return rs.getString(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Calendar getSentTime(int orderId) {
        Connection connection = DB.getInstance().getConnection();
        String getState = "select SentTime from [Order] where idOrder = " + orderId + " ";
        try(Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(getState)) {
            Calendar cal = Calendar.getInstance();
            cal.clear();
            if(rs.next()) {
                Timestamp dat =  rs.getTimestamp(1);
                if(dat == null) return null;
                cal.setTimeInMillis(dat.getTime());
                return cal;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Calendar getRecievedTime(int orderId) {
        Connection connection = DB.getInstance().getConnection();
        String getState = "select ReceivedTime from [Order] where idOrder = " + orderId + " ";
        try(Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(getState)) {
            Calendar cal = Calendar.getInstance();
            if(rs.next()) {
                Timestamp dat =  rs.getTimestamp(1);
                if(dat == null) return null;
                cal.setTimeInMillis(dat.getTime());
                return cal;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getBuyer(int orderId) {
        Connection connection = DB.getInstance().getConnection();
        String getState = "select idBuyer from [Order] where idOrder = " + orderId + " ";
        try(Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(getState)) {
            if(rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public int getLocation(int orderId) {
        Connection connection = DB.getInstance().getConnection();
        String getTransport = "select l.CityFrom from Transport t, Line l where t.idOrder = " + orderId +
                            " and t.idItem is NULL and t.CurrentLine = l.idLine ";
        try(Statement st = connection.createStatement();
            ResultSet fromTransport = st.executeQuery(getTransport)) {
            if(fromTransport.next()) return fromTransport.getInt(1);
            //If the whole order was not found in the Trasnport table
            String getAssemblyLoc = "select AssemblyCity, [State] from [Order] where idOrder = " + orderId;
            try(Statement st1 = connection.createStatement();
                ResultSet assemblyLoc = st1.executeQuery(getAssemblyLoc)) {
                if(assemblyLoc.next()) {
                    String state = assemblyLoc.getString(2);
                    if(state.equals("created")) return -1;
                    if(state.equals("sent")) return assemblyLoc.getInt(1);
                    //else the state is arrived so the location is the city of the buyer
                    String getBuyerCity = "select b.idCity from Buyer b, [Order] o where o.idBuyer = b.idBuyer and o.idOrder = " + orderId + " ";
                    try(Statement st2 = connection.createStatement();
                        ResultSet buyersCity = st2.executeQuery(getBuyerCity)) {
                        if(buyersCity.next()) {
                            int retVal = buyersCity.getInt(1);
                            return retVal;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

}
