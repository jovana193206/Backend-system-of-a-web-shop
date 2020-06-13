package student;

import operations.ArticleOperations;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class mj150206_ArticleOperations implements ArticleOperations {

    @Override
    public int createArticle(int shopId, String articleName, int articlePrice) {
        Connection connection = DB.getInstance().getConnection();
        String insertQuery = "insert into Article (Name, Price, idShop) values(?,?, ?) ";
        ResultSet rs = null;
        try (PreparedStatement ps = connection.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, articleName);
            ps.setBigDecimal(2, new BigDecimal(articlePrice));
            ps.setInt(3, shopId);
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

}
