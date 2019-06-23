
package res;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class DatabaseHelper {
    public static long Bill_Id = 0;
    private Connection con;
    private final String TABLE_ITEMS = "ITEMS", TABLE_BILLS = "BILLS", TABLE_BILL_ITEM = "BILL_ITEM";
    
    public DatabaseHelper() throws SQLException{
        try{
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            con = DriverManager.getConnection("jdbc:derby://localhost:1527/AnitaDresses", "mayur", "user1");
            ResultSet res = con.getMetaData().getTables(null, "MAYUR", TABLE_ITEMS.toUpperCase(), null);
            if(!res.next()){
                Statement st= con.createStatement();            
                st.execute("CREATE TABLE " + TABLE_ITEMS + "(Id BIGINT GENERATED ALWAYS AS IDENTITY (START WITH 100001, INCREMENT BY 1), "
                        + "Descr VARCHAR(35) NOT NULL, "
                        + "Price INT NOT NULL, "
                        + "CONSTRAINT item_id PRIMARY KEY (Id))");
                st.execute("CREATE TABLE " + TABLE_BILLS + "(Id BIGINT, "
                        + "Bill_Date Date NOT NULL, "
                        + "Customer_Name VARCHAR(30), "
                        + "Mobile_No VARCHAR(15), "
                        + "Total_Amount DECIMAL(15, 2) NOT NULL, "
                        + "GST DECIMAL(3, 2) NOT NULL, "
                        + "Discount DECIMAL(15), "
                        + "CONSTRAINT bill_id PRIMARY KEY (Id))");
                st.execute("CREATE TABLE " + TABLE_BILL_ITEM + "(Bill_Id BIGINT NOT NULL, "
                        + "Item_Id BIGINT NOT NULL, "
                        + "Quantity INT NOT NULL, "
                        + "CONSTRAINT bill_id_ref FOREIGN KEY (Bill_Id) REFERENCES " + TABLE_BILLS + "(Id), "
                        + "CONSTRAINT item_id_ref FOREIGN KEY (Item_Id) REFERENCES " + TABLE_ITEMS + "(Id), "
                        + "CONSTRAINT bill_item PRIMARY KEY (Bill_Id, Item_Id))");
                st.close();
                con.commit();
            }
        }
        catch(Exception ex){
            Utils.errorMessage(ex);
            con.rollback();
        }
    }
    
    public int insertInItems(String desc, int price) throws SQLException{
        try{
            PreparedStatement ps = con.prepareStatement("INSERT INTO Items(Descr, Price) VALUES(?, ?)");
            ps.setString(1, desc);
            ps.setInt(2, price);
            if(ps.executeUpdate() == 1){
                ResultSet rs = getItems();
                rs.last();
                return rs.getInt("Id");
            }
            ps.close();
            con.commit();
        }
        catch(Exception ex){
            Utils.errorMessage(ex);
            con.rollback();
        }
        return 0;
    }
    
    public void clear(){
        try {
            Statement st= con.createStatement();
            st.executeUpdate("DELETE FROM " + TABLE_BILL_ITEM);
            con.commit();
            
            st.executeUpdate("DELETE FROM " + TABLE_BILLS);
            con.commit();
            
            st.executeUpdate("DELETE FROM " + TABLE_ITEMS);
            con.commit();
            
            st.close();
        } catch (Exception ex) {
            Utils.errorMessage(ex);
        }
    }
    
    public ResultSet getTableData(String table_name){
        try {
            return con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery("SELECT * FROM " + table_name);
        } catch (Exception ex) {
            Utils.errorMessage(ex);
        }
        return null;
    }
    
    public ResultSet getItems(){
        try {
            return con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery("SELECT descr, price, id FROM " + TABLE_ITEMS);
        } catch (Exception ex) {
            Utils.errorMessage(ex);
        }
        return null;
    }
    
    public ResultSet getBills(){
        try {
            return con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery("SELECT Bill_Date, Customer_Name, Total_Amount FROM " + TABLE_BILLS);
        } catch (Exception ex) {
            Utils.errorMessage(ex);
        }
        return null;
    }
    
    public long getBillId(){
        try {
            ResultSet rs = getTableData(TABLE_BILLS);
            if(!rs.last()){
                return 100001;
            }
            else{
                long newId = rs.getLong("Id") + 1;
                PreparedStatement ps = con.prepareStatement("SELECT Id FROM " + TABLE_BILLS + " WHERE Id= ?");
                ps.setLong(1, newId);
                ResultSet temp_rs = ps.executeQuery();
                while(temp_rs.next()){
                    newId =  temp_rs.getLong("Id") + 1;
                    ps.setLong(1, newId);
                    temp_rs = ps.executeQuery();
                }
                return newId;
            }
        }catch (Exception ex) {
            Utils.errorMessage(ex);
        }
        return 0;
    }
    
    public Bill getBillData(long billId){
        Bill billData = new Bill();
        try{
            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM " + TABLE_BILLS + " WHERE Id = " + billId);
            rs.next();
            billData.Bill_Id = rs.getBigDecimal("Id").toBigInteger();
            billData.Bill_Date = rs.getDate("Bill_Date");
            billData.Customer_Name = rs.getString("Customer_Name");
            billData.Mobile_No = rs.getString("Mobile_No");
            billData.GST = rs.getBigDecimal("GST");
            billData.Discount = rs.getBigDecimal("Discount");
            billData.Total_Amount = rs.getBigDecimal("Total_Amount");
            billData.Items = new ArrayList<Bill_Item>();
            
            rs = con.createStatement().executeQuery("SELECT * FROM " + TABLE_BILL_ITEM + " WHERE Bill_Id = " + billId);
            while(rs.next()){
                ResultSet item_rs = con.createStatement().executeQuery("SELECT * FROM " + TABLE_ITEMS + " WHERE Id = " + (rs.getBigDecimal("Item_Id").toBigInteger().toString()));
                item_rs.next();
                Bill_Item item = new Bill_Item();
                item.Item_Id = rs.getBigDecimal("Item_Id").toBigInteger();
                item.Description = item_rs.getString("Descr");
                item.Quantity = rs.getInt("Quantity");
                item.UPrice = item_rs.getBigDecimal("Price");
                item.TPrice = BigDecimal.valueOf(item.Quantity).multiply(item.UPrice);
                billData.Items.add(item);
            }
        }
        catch(Exception ex){
            Utils.errorMessage(ex);
        }
        return billData;
    }
    
    public void saveBill(Bill bill) throws SQLException{
        try {
            PreparedStatement ps;
            ResultSet rs =  con.createStatement().executeQuery("SELECT * FROM " + TABLE_BILLS + " WHERE Id = " + bill.Bill_Id);
            if(rs.next()){
                ps = con.prepareStatement("UPDATE " + TABLE_BILLS + " SET Bill_Date = ?, Customer_Name = ?, Mobile_No = ?, Total_Amount = ?, GST = ?, Discount = ? WHERE ID = ?");
                ps.setInt(7, bill.Bill_Id.intValue());
                ps.setDate(1, bill.Bill_Date);
                ps.setString(2, bill.Customer_Name);
                ps.setString(3, bill.Mobile_No);
                ps.setBigDecimal(4, bill.Total_Amount);
                ps.setBigDecimal(5, bill.GST);
                ps.setBigDecimal(6, bill.Discount);
                ps.executeUpdate();
                ps.close();
                con.commit();
                Statement st = con.createStatement();
                st.executeUpdate("DELETE FROM " + TABLE_BILL_ITEM + " WHERE Bill_Id = " + bill.Bill_Id.intValue());
                st.close();
                con.commit();
            }else{
                ps = con.prepareStatement("INSERT INTO " + TABLE_BILLS + " VALUES(?, ?, ?, ?, ?, ?, ?)");
                ps.setInt(1, bill.Bill_Id.intValue());
                ps.setDate(2, bill.Bill_Date);
                ps.setString(3, bill.Customer_Name);
                ps.setString(4, bill.Mobile_No);
                ps.setBigDecimal(5, bill.Total_Amount);
                ps.setBigDecimal(6, bill.GST);
                ps.setBigDecimal(7, bill.Discount);
                ps.executeUpdate();
                ps.close();
                con.commit();
            }

            for(int i = 0; i < bill.Items.size(); i++){
                Bill_Item item = bill.Items.get(i);
                if(item.Item_Id.intValue() == 0){
                    item.Item_Id = BigInteger.valueOf(insertInItems(item.Description, item.UPrice.intValue()));
                }
                ps = con.prepareStatement("INSERT INTO " + TABLE_BILL_ITEM + " VALUES(?, ?, ?)");
                ps.setInt(1, bill.Bill_Id.intValue());
                ps.setInt(2, item.Item_Id.intValue());
                ps.setInt(3, item.Quantity);
                ps.executeUpdate();
                ps.close();
                con.commit();
            }
        } catch (SQLException ex) {
            Utils.errorMessage(ex);
            con.rollback();
        }
    }
    
    public int getBillIdByIndex(int index){
        try {
            ResultSet rs = getTableData(TABLE_BILLS);
            rs.afterLast();
            for(int i=0; i<=index; i++){
                rs.previous();
            }
            return rs.getInt("Id");
        } catch (SQLException ex) {
            Utils.errorMessage(ex);
        }
        return 0;
    }
    
    public int getItemIdByIndex(int index){
        try {
            ResultSet rs = getTableData(TABLE_ITEMS);
            for(int i=0; i <= index; i++){
                rs.next();
            }
            return rs.getInt("Id");
        } catch (SQLException ex) {
            Utils.errorMessage(ex);
        }
        return 0;
    }
    
    public boolean deleteBill(int id){
        Statement st = null;
        try{
            st = con.createStatement();
            st.executeUpdate("DELETE FROM " + TABLE_BILL_ITEM + " WHERE Bill_Id = " + id);
            if(st.executeUpdate("DELETE FROM " + TABLE_BILLS + " WHERE Id = " + id) == 1){
                return true;
            }
            st.close();
            con.commit();
        }
        catch(Exception ex){}
        finally{
            try {
                st.close();
                con.commit();
            } catch (SQLException ex) {}
        }
        return false;
    }
    
    public boolean deleteItem(int id){
        Statement st = null;
        try{
            st = con.createStatement();
            if(st.executeUpdate("DELETE FROM " + TABLE_ITEMS + " WHERE Id = " + id) == 1){
                return true;
            }
        }
        catch(Exception ex){
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Cannot delete the item because the item is present in one or more bills.");

            alert.showAndWait();
        }
        finally{
            try {
                st.close();
                con.commit();
            } catch (SQLException ex) {}
        }
        return false;
    }
    
    
    public Connection getConnection(){
        return con;
    }
    
    public void close(){
        try {
            if(!con.isClosed())
                con.close();
        } catch (Exception ex) {
            Utils.errorMessage(ex);
        }
    }
}
