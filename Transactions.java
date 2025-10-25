package com.misys.tiplus2.customisation.pane;

import com.misys.tiplus2.customisation.entity.ExtEventTransactionsEntityWrapper;
import com.misys.tiplus2.customisation.entity.ExtEventTransactions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Transaction extends EventPane {

    private static final long serialVersionUID = -4680177734752262824L;
    private static final Logger LOG = LoggerFactory.getLogger(Transaction.class);

    /**
     * Triggered when 'Fetch Transactions' button is clicked
     */
    @Override
    public void onFetchTransactionTransactionButton() {
        LOG.info("Fetch Transactions button clicked...");
        fetchTransactionDataFromDB();

    }

    /**
     * Fetch data from TRANSACTIONS table and populate TI+ table
     */
    public void fetchTransactionDataFromDB()
 {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            Context initContext = new InitialContext();
            DataSource ds = (DataSource) initContext.lookup("jdbc/zone"); // Check your JNDI name
            connection = ds.getConnection();

            // ✅ Correct SQL for your table
            String sql = "SELECT TXNID, CURRENCY, AMOUNT, STATUS FROM TRANSACTIONS";
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();

            LOG.info("Fetching data from TRANSACTIONS table...");

            boolean found = false;
            while (rs.next()) {
                found = true;

                ExtEventTransactions tx = new ExtEventTransactions();

                tx.setTxnId(rs.getString("TXNID").trim());
                tx.setCurrency(rs.getString("CURRENCY").trim());

                // Handle AMOUNT as string since your column type is CHAR(12)
                String amountStr = rs.getString("AMOUNT");
                if (amountStr != null && !amountStr.trim().isEmpty()) {
                    tx.setAmount(Integer.valueOf(amountStr.trim()));
                } else {
                    tx.setAmount(0);
                }

                tx.setStatus(rs.getString("STATUS").trim());

                ExtEventTransactionsEntityWrapper wrapper =
                        new ExtEventTransactionsEntityWrapper(tx, getDriverWrapper());
                this.addNewExtEventTransactions(wrapper);

                LOG.info("Fetched TXNID={}, CURRENCY={}, AMOUNT={}, STATUS={}",
                        tx.getTxnId(), tx.getCurrency(), tx.getAmount(), tx.getStatus());
            }

            if (!found) {
                LOG.info("No transaction data found in TRANSACTIONS table.");
            }

        } catch (Exception e) {
            LOG.error("Error while fetching data from TRANSACTIONS: ", e);
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
            try { if (connection != null) connection.close(); } catch (Exception ignored) {}
        }
    }
}
