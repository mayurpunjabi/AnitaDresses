/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package res;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.util.ArrayList;

public class Bill {
    public BigInteger Bill_Id;
    public Date Bill_Date;
    public String Customer_Name, Mobile_No;
    public ArrayList<Bill_Item> Items;
    public BigDecimal GST = BigDecimal.valueOf(5), Discount, Total_Amount;
}
