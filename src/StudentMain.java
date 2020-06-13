import operations.*;
import org.junit.Test;
import student.*;
import tests.TestHandler;
import tests.TestRunner;

import java.util.Calendar;

public class StudentMain {

    public static void main(String[] args) {

        // Change this for your implementation (points will be negative if interfaces are not implemented).
        ArticleOperations articleOperations = new mj150206_ArticleOperations();
        BuyerOperations buyerOperations = new mj150206_BuyerOperations();
        CityOperations cityOperations = new mj150206_CityOperations();
        GeneralOperations generalOperations = new mj150206_GeneralOperations();
        OrderOperations orderOperations = new mj150206_OrderOperations(generalOperations);
        ShopOperations shopOperations = new mj150206_ShopOperations();
        TransactionOperations transactionOperations = new mj150206_TransactionOperations();
//
//        Calendar c = Calendar.getInstance();
//        c.clear();
//        c.set(2010, Calendar.JANUARY, 01);
//
//
//        Calendar c2 = Calendar.getInstance();
//        c2.clear();
//        c2.set(2010, Calendar.JANUARY, 01);
//
//        if(c.equals(c2)) System.out.println("jednako");
//        else System.out.println("nije jednako");

        TestHandler.createInstance(
                articleOperations,
                buyerOperations,
                cityOperations,
                generalOperations,
                orderOperations,
                shopOperations,
                transactionOperations
        );

        TestRunner.runTests();
    }
}
