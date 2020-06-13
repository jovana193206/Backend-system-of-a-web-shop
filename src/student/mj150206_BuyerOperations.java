package student;

import operations.BuyerOperations;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class mj150206_BuyerOperations implements BuyerOperations {

    @Override
    public int createBuyer(String name, int cityId) {
        Connection connection = DB.getInstance().getConnection();
        String insertQuery = "insert into Buyer (Name, idCity) values(?,?) ";
        ResultSet rs = null;
        try (PreparedStatement ps = connection.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setInt(2, cityId);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if(rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        finally {
            if(rs != null) try {rs.close();} catch (SQLException ex1){}
        }
        return -1;
    }

    @Override
    public int setCity(int buyerId, int cityId) {
        Connection connection = DB.getInstance().getConnection();
        String updateQuery = "update Buyer set idCity = ? where idBuyer = ? ";
        try (PreparedStatement ps = connection.prepareStatement(updateQuery)) {
            ps.setInt(1, cityId);
            ps.setInt(2, buyerId);
            int updated = ps.executeUpdate();
            if(updated != 1) return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return 1;
    }

    @Override
    public int getCity(int buyerId) {
        Connection connection = DB.getInstance().getConnection();
        String query = "select idCity from Buyer where idBuyer = ? ";
        ResultSet rs = null;
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, buyerId);
            rs = ps.executeQuery();
            if(rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        finally {
            if(rs != null) try {rs.close();} catch (SQLException ex1){}
        }
        return -1;
    }

    @Override
    public BigDecimal increaseCredit(int buyerId, BigDecimal credit) {
        Connection connection = DB.getInstance().getConnection();
        String getCredit = "select Credit from Buyer where idBuyer = " + buyerId + " ";
        String updateQuery = "update Buyer set credit = ? where idBuyer = ? ";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(getCredit);
             PreparedStatement ps = connection.prepareStatement(updateQuery)) {
            BigDecimal currentCredit = BigDecimal.ZERO;
            if(rs.next()) {
                currentCredit = rs.getBigDecimal(1);
            }
            else return new BigDecimal(-1);
            BigDecimal newCredit = currentCredit.add(credit);
            ps.setBigDecimal(1, newCredit);
            ps.setInt(2, buyerId);
            int updated = ps.executeUpdate();
            if(updated != 1) return new BigDecimal(-1);
            return newCredit.setScale(3);
        } catch (SQLException e) {
            e.printStackTrace();
            return new BigDecimal(-1);
        }
    }

    @Override
    public int createOrder(int buyerId) {
        Connection connection = DB.getInstance().getConnection();
        String insertQuery = "insert into [Order] (idBuyer) values(?) ";
        ResultSet rs = null;
        try (PreparedStatement ps = connection.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, buyerId);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if(rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        finally {
            if(rs != null) try {rs.close();} catch (SQLException ex1){}
        }
        return -1;
    }

    @Override
    public List<Integer> getOrders(int buyerId) {
        Connection connection = DB.getInstance().getConnection();
        String query = "select idOrder from [Order] where idBuyer = " + buyerId + " ";
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
    public BigDecimal getCredit(int buyerId) {
        Connection connection = DB.getInstance().getConnection();
        String query = "select Credit from Buyer where idBuyer = ? ";
        ResultSet rs = null;
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, buyerId);
            rs = ps.executeQuery();
            if(rs.next()) return rs.getBigDecimal(1).setScale(3);
        } catch (SQLException e) {
            e.printStackTrace();
            return new BigDecimal(-1);
        }
        finally {
            if(rs != null) try {rs.close();} catch (SQLException ex1){}
        }
        return new BigDecimal(-1);
    }
}
