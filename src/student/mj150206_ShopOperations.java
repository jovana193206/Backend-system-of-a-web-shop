package student;

import operations.ShopOperations;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class mj150206_ShopOperations implements ShopOperations {

    @Override
    public int createShop(String name, String cityName) {
        Connection connection = DB.getInstance().getConnection();
        String insertQuery = "insert into Shop (Name, idCity) values(?, ?) ";
        String getCityID = "select idCity from City where Name = '" + cityName + "' ";
        ResultSet rs = null;
        try (Statement st = connection.createStatement();
             ResultSet city = st.executeQuery(getCityID);
             PreparedStatement ps = connection.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
            int cityId = 0;
            if(city.next()) {
                cityId = city.getInt(1);
            }
            else {  //The city with cityName was not found
                return -1;
            }
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
    public int setCity(int shopId, String cityName) {
        Connection connection = DB.getInstance().getConnection();
        String updateQuery = "update Shop set idCity = ? where idShop = ? ";
        String getCityID = "select idCity from City where Name = '" + cityName + "' ";
        try (Statement st = connection.createStatement();
             ResultSet city = st.executeQuery(getCityID);
             PreparedStatement ps = connection.prepareStatement(updateQuery)) {
            int cityId = 0;
            if(city.next()) {
                cityId = city.getInt(1);
            }
            else {  //The city with cityName was not found
                return -1;
            }
            ps.setInt(1, cityId);
            ps.setInt(2, shopId);
            int updated = ps.executeUpdate();
            if(updated != 1) {
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return 1;
    }

    @Override
    public int getCity(int shopId) {
        Connection connection = DB.getInstance().getConnection();
        String getCity = "select idCity from Shop where idShop = " + shopId + " ";
        try (Statement st = connection.createStatement();
             ResultSet city = st.executeQuery(getCity)) {
            if(city.next()) {
                return city.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return -1;
    }

    @Override
    public int setDiscount(int shopId, int discountPercentage) {
        Connection connection = DB.getInstance().getConnection();
        String updateQuery = "update Shop set Discount = ? where idShop = ? ";
        try(PreparedStatement ps = connection.prepareStatement(updateQuery)) {
            ps.setInt(1, discountPercentage);
            ps.setInt(2, shopId);
            int updated = ps.executeUpdate();
            if(updated != 1) {
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return 1;
    }

    @Override
    public int increaseArticleCount(int articleId, int increment) {
        Connection connection = DB.getInstance().getConnection();
        String getCount = "select Count from Article where idArticle = " + articleId + " ";
        String updateQuery = "update Article set Count = ? where idArticle = ? ";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(getCount);
             PreparedStatement ps = connection.prepareStatement(updateQuery)) {
            int currentCount = 0;
            if(rs.next()) {
                currentCount = rs.getInt(1);
            }
            else return -1;
            int newCount = currentCount + increment;
            ps.setInt(1, newCount);
            ps.setInt(2, articleId);
            int updated = ps.executeUpdate();
            if(updated != 1) return -1;
            return newCount;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public int getArticleCount(int articleId) {
        Connection connection = DB.getInstance().getConnection();
        String getCity = "select Count from Article where idArticle = " + articleId + " ";
        try (Statement st = connection.createStatement();
             ResultSet count = st.executeQuery(getCity)) {
            if(count.next()) {
                return count.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return -1;
    }

    @Override
    public List<Integer> getArticles(int shopId) {
        Connection connection = DB.getInstance().getConnection();
        String query = "select idArticle from Article where idShop = " + shopId + " ";
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
    public int getDiscount(int shopId) {
        Connection connection = DB.getInstance().getConnection();
        String getDiscount = "select Discount from Shop where idShop = " + shopId + " ";
        try (Statement st = connection.createStatement();
             ResultSet discount = st.executeQuery(getDiscount)) {
            if(discount.next()) {
                return discount.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return -1;
    }
}
