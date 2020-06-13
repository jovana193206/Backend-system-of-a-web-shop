package student;

import operations.CityOperations;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class mj150206_CityOperations implements CityOperations {

    @Override
    public int createCity(String name) {
        Connection connection = DB.getInstance().getConnection();
        String insertQuery = "insert into City (Name) values(?) ";
        ResultSet rs = null;
        try (PreparedStatement ps = connection.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
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
    public List<Integer> getCities() {
        Connection connection = DB.getInstance().getConnection();
        String query = "select idCity from City order by idCity";
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
    public int connectCities(int cityId1, int cityId2, int distance) {
        Connection connection = DB.getInstance().getConnection();
        String insertQuery = "insert into Line (CityFrom, CityTo, Distance) values(?, ?, ?) ";
        Statement st = null;
        ResultSet rs = null;
        try (PreparedStatement ps = connection.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, cityId1);
            ps.setInt(2, cityId2);
            ps.setInt(3, distance);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            int retVal = -1;
            if(rs.next()) {
                retVal = rs.getInt(1);
                ps.setInt(1, cityId2);
                ps.setInt(2, cityId1);
                ps.setInt(3, distance);
                ps.executeUpdate();
                rs = ps.getGeneratedKeys();
                 if(!rs.next()) {
                     st = connection.createStatement();
                     st.executeUpdate("delete from Line where idLine = " + retVal + " ");
                     retVal = -1;
                 }
            }
            return retVal;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        finally {
            if(rs != null) try {rs.close();} catch (SQLException ex1){}
            if(st != null) try {st.close();} catch (SQLException ex1){}
        }
    }

    @Override
    public List<Integer> getConnectedCities(int cityId) {
        Connection connection = DB.getInstance().getConnection();
        String query = "select CityTo from Line where CityFrom = " + cityId  + " ";
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
    public List<Integer> getShops(int cityId) {
        Connection connection = DB.getInstance().getConnection();
        String query = "select idShop from Shop where idCity = " + cityId + " ";
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
}
