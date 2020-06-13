package student;

import operations.TransactionOperations;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class mj150206_TransactionOperations implements TransactionOperations {

    @Override
    public BigDecimal getBuyerTransactionsAmmount(int buyerId) {
        Connection connection = DB.getInstance().getConnection();
        String getSum = "select sum(T.Amount) from [Transaction] T, [Order] O where " +
                "T.idOrder = O.idOrder and O.idBuyer = " + buyerId + " and T.idShop is NULL ";
        try(Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(getSum)) {
            if(rs.next()) {
                return rs.getBigDecimal(1).setScale(3);
            }
            else return new BigDecimal(0).setScale(3);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new BigDecimal(-1);
    }

    @Override
    public BigDecimal getShopTransactionsAmmount(int shopId) {
        Connection connection = DB.getInstance().getConnection();
        String getSum = "select sum(Amount) from [Transaction] where idShop = " + shopId + " ";
        try(Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(getSum)) {
            if(rs.next()) {
                BigDecimal retVal = rs.getBigDecimal(1);
                if(retVal == null) return new BigDecimal(0).setScale(3);
                else return retVal.setScale(3);
            }
            else return new BigDecimal(0).setScale(3);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new BigDecimal(-1);
    }

    @Override
    public List<Integer> getTransationsForBuyer(int buyerId) {
        Connection connection = DB.getInstance().getConnection();
        String query = "select T.idTransaction from [Transaction] T, [Order] O where T.idOrder = O.idOrder "
                + "and O.idBuyer = " + buyerId + " and T.idShop is NULL ";
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
    public int getTransactionForBuyersOrder(int orderId) {
        Connection connection = DB.getInstance().getConnection();
        String getTrans = "select idTransaction from [Transaction] where idOrder = " + orderId + " and idShop is NULL ";
        try(Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(getTrans)) {
            if(rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public int getTransactionForShopAndOrder(int orderId, int shopId) {
        Connection connection = DB.getInstance().getConnection();
        String getTrans = "select idTransaction from [Transaction] where idOrder = " + orderId + " and idShop = " + shopId + " ";
        try(Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(getTrans)) {
            if(rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public List<Integer> getTransationsForShop(int shopId) {
        Connection connection = DB.getInstance().getConnection();
        String query = "select idTransaction from [Transaction] where idShop = " + shopId + " ";
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
    public Calendar getTimeOfExecution(int transactionId) {
        Connection connection = DB.getInstance().getConnection();
        String getState = "select ExecutionTime from [Transaction] where idTransaction = " + transactionId + " ";
        try(Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(getState)) {
            Calendar cal = Calendar.getInstance();
            cal.clear();
            if(rs.next()) {
                Date dat =  rs.getDate(1);
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
    public BigDecimal getAmmountThatBuyerPayedForOrder(int orderId) {
        Connection connection = DB.getInstance().getConnection();
        String getSum = "select Amount from [Transaction] where idOrder = " + orderId + " and idShop is NULL ";
        try(Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(getSum)) {
            if(rs.next()) {
                return rs.getBigDecimal(1).setScale(3);
            }
            else return new BigDecimal(0).setScale(3);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new BigDecimal(-1);
    }

    @Override
    public BigDecimal getAmmountThatShopRecievedForOrder(int shopId, int orderId) {
        Connection connection = DB.getInstance().getConnection();
        String getSum = "select sum(Amount) from [Transaction] where idOrder = " + orderId + " and idShop = " + shopId + " ";
        try(Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(getSum)) {
            if(rs.next()) {
                return rs.getBigDecimal(1).setScale(3);
            }
            else return new BigDecimal(0).setScale(3);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new BigDecimal(-1);
    }

    @Override
    public BigDecimal getTransactionAmount(int transactionId) {
        Connection connection = DB.getInstance().getConnection();
        String getSum = "select Amount from [Transaction] where idTransaction = " + transactionId + " ";
        try(Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(getSum)) {
            if(rs.next()) {
                return rs.getBigDecimal(1).setScale(3);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new BigDecimal(-1);
    }

    @Override
    public BigDecimal getSystemProfit() {
        Connection connection = DB.getInstance().getConnection();
        String getIncome = "select sum(T.Amount) from [Transaction] T where T.idShop is NULL and T.ExecutionTime is not NULL " +
                "and 'arrived' = (select [State] from [Order] O where O.idOrder = T.idOrder) ";
        String getOutcome = "select sum(Amount) from [Transaction] where idShop is not NULL and ExecutionTime is not NULL ";
        try(Statement stIn = connection.createStatement();
            ResultSet rsIn = stIn.executeQuery(getIncome);
            Statement stOut = connection.createStatement();
            ResultSet rsOut = stOut.executeQuery(getOutcome)) {
            if(rsIn.next()) {
                if(rsOut.next()) {
                    if(rsIn.getBigDecimal(1) == null) return new BigDecimal(0).setScale(3);
                    return rsIn.getBigDecimal(1).subtract(rsOut.getBigDecimal(1)).setScale(3);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
