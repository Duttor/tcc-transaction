package io.anyway.galaxy.repository.impl;

import com.google.common.base.Strings;
import io.anyway.galaxy.common.Constants;
import io.anyway.galaxy.common.TransactionStatusEnum;
import io.anyway.galaxy.common.TransactionTypeEnum;
import io.anyway.galaxy.domain.TransactionInfo;
import io.anyway.galaxy.exception.DistributedTransactionException;
import io.anyway.galaxy.spring.DataSourceAdaptor;
import io.anyway.galaxy.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiong.j on 2016/7/21.
 */
@Repository
public class JdbcTransactionRepository extends CacheableTransactionRepository {

	private static final String PG_DATE_SQL = "current_timestamp(0)::timestamp without time zone";

	private static final String ORACLE_DATE_SQL = "sysdate";

	private static final String MYSQL_DATE_SQL="current_timestamp()";

	private static final String SELECT_DQL = "SELECT TX_ID, PARENT_ID, MODULE_ID, BUSINESS_ID, BUSINESS_TYPE, TX_TYPE, TX_STATUS, CONTEXT, RETRIED_COUNT, NEXT_RETRY_TIME, GMT_CREATED, GMT_MODIFIED";

    @Value("${tx.begin.timeout.second}")
    private int beginTimeoutSecond = 600;

    @Autowired
	private DataSourceAdaptor dataSourceAdaptor;

	protected int doCreate(TransactionInfo transactionInfo) throws SQLException  {

		Connection conn= DataSourceUtils.getConnection(dataSourceAdaptor.getDataSource());

		PreparedStatement stmt = null;

		try {

			StringBuilder builder = new StringBuilder();
			builder.append("INSERT INTO TRANSACTION_INFO " + "(TX_ID, PARENT_ID, BUSINESS_ID, BUSINESS_TYPE, TX_TYPE"
					+ ", TX_STATUS, CONTEXT, RETRIED_COUNT, MODULE_ID, GMT_CREATED" + ", GMT_MODIFIED)"
					+ " VALUES(?,?,?,?,?" + ",?,?,?,?," + getDateSql(conn) + ", " + getDateSql(conn) + ")");

			stmt = conn.prepareStatement(builder.toString());

			stmt.setLong(1, transactionInfo.getTxId());
			stmt.setLong(2, transactionInfo.getParentId());
			stmt.setString(3, transactionInfo.getBusinessId());
			stmt.setString(4, transactionInfo.getBusinessType());
			stmt.setInt(5, transactionInfo.getTxType());
			stmt.setInt(6, transactionInfo.getTxStatus());
			stmt.setString(7, transactionInfo.getContext());
			stmt.setString(8, transactionInfo.getRetriedCount());
			stmt.setString(9,transactionInfo.getModuleId());

			return stmt.executeUpdate();

		} catch (Throwable e) {
			throw new DistributedTransactionException(e);
		} finally {
			closeStatement(stmt);
			releaseConnection(conn);
		}
	}

	protected int doUpdate(TransactionInfo transactionInfo) {

		Connection conn= DataSourceUtils.getConnection(dataSourceAdaptor.getDataSource());
		PreparedStatement stmt = null;

		try {

			StringBuilder builder = new StringBuilder();
			builder.append("UPDATE TRANSACTION_INFO SET ");
			if (transactionInfo.getTxType() != -1) {
				builder.append("TX_TYPE = ?, ");
			}
			if (transactionInfo.getTxStatus() != -1) {
				builder.append("TX_STATUS = ?, ");
			}
			if (!Strings.isNullOrEmpty(transactionInfo.getContext())) {
				builder.append("CONTEXT = ?, ");
			}
			if (transactionInfo.getNextRetryTime() != null) {
				builder.append("NEXT_RETRY_TIME = ?, ");
			}
			if (!Strings.isNullOrEmpty(transactionInfo.getRetriedCount())) {
				builder.append("RETRIED_COUNT = ?, ");
			}
			builder.append("GMT_MODIFIED = " + getDateSql(conn));

			builder.append(" WHERE 1=1");
			if (transactionInfo.getTxId() > -1L) {
				builder.append(" AND TX_ID = ? ");
			}
			if (transactionInfo.getParentId() > -1L) {
				builder.append(" AND PARENT_ID = ? ");
			}

			stmt = conn.prepareStatement(builder.toString());

			int condition = 0;

			if (transactionInfo.getTxType() != -1) {
				stmt.setInt(++condition, transactionInfo.getTxType());
			}
			if (transactionInfo.getTxStatus() != -1) {
				stmt.setInt(++condition, transactionInfo.getTxStatus());
			}
			if (!Strings.isNullOrEmpty(transactionInfo.getContext())) {
				stmt.setString(++condition, transactionInfo.getContext());
			}
			if (transactionInfo.getNextRetryTime() != null) {
				stmt.setTimestamp(++condition, new Timestamp(transactionInfo.getNextRetryTime().getTime()));
			}
			if (!Strings.isNullOrEmpty(transactionInfo.getRetriedCount())) {
				stmt.setString(++condition, transactionInfo.getRetriedCount());
			}
			if (transactionInfo.getTxId() > -1L) {
				stmt.setLong(++condition, transactionInfo.getTxId());
			}
			if (transactionInfo.getParentId() > -1L) {
				stmt.setLong(++condition, transactionInfo.getParentId());
			}
			int result = stmt.executeUpdate();

			return result;

		} catch (Throwable e) {
			throw new DistributedTransactionException(e);
		} finally {
			closeStatement(stmt);
			releaseConnection(conn);
		}
	}

	protected int doDelete(TransactionInfo transactionInfo) {

		Connection conn= DataSourceUtils.getConnection(dataSourceAdaptor.getDataSource());
		PreparedStatement stmt = null;

		try {

			StringBuilder builder = new StringBuilder();
			builder.append("DELETE FROM TRANSACTION_INFO " + " WHERE TX_ID = ?");

			stmt = conn.prepareStatement(builder.toString());

			stmt.setLong(1, transactionInfo.getTxId());

			return stmt.executeUpdate();

		} catch (SQLException e) {
			throw new DistributedTransactionException(e);
		} finally {
			closeStatement(stmt);
			releaseConnection(conn);
		}
	}

	@Override
	protected List<TransactionInfo> doFindSince(Date date, Integer[] txStatus, String moduleId) {

		Connection conn= DataSourceUtils.getConnection(dataSourceAdaptor.getDataSource());

		List<TransactionInfo> transactionInfos = new ArrayList<TransactionInfo>();

		PreparedStatement stmt = null;

		try {

			StringBuilder builderOuter = new StringBuilder();
			builderOuter.append(SELECT_DQL + " FROM (");

			StringBuilder builder = new StringBuilder();
			builder.append(SELECT_DQL + " FROM TRANSACTION_INFO WHERE GMT_CREATED > ? ");
					/*.append(getLimitSql(conn, 1000))*/

			boolean hasTriedStatus = false;
			StringBuilder sBuilder = new StringBuilder();
			sBuilder.append(" AND TX_STATUS ");
			if (txStatus.length > 1) {
				sBuilder.append(" IN (");
				for (int i = 0; i < txStatus.length; i++) {
					if (txStatus[i] != TransactionStatusEnum.TRIED.getCode()) {
						if (i == 0) {
							sBuilder.append(txStatus[i]);
						} else {
							sBuilder.append(",").append(txStatus[i]);
						}
					} else {
						hasTriedStatus = true;
					}
				}
				sBuilder.append(")");
			} else if (txStatus.length == 1) {
				if (txStatus[0] != TransactionStatusEnum.TRIED.getCode()) {
					sBuilder.append(" = ");
					sBuilder.append(txStatus[0]);
				} else {
					hasTriedStatus = true;
				}
			}

			builder.append(" AND MODULE_ID = ? ");

			builderOuter.append(builder).append(" AND NEXT_RETRY_TIME <= ? AND NEXT_RETRY_TIME IS NOT NULL").append(sBuilder);
			builderOuter.append(" UNION ALL ").append(builder).append("AND NEXT_RETRY_TIME IS NULL").append(sBuilder);
			if (hasTriedStatus) {
				builderOuter.append(" UNION ALL ").append(builder).append(" AND NEXT_RETRY_TIME <= ? AND NEXT_RETRY_TIME IS NOT NULL")
						.append(" AND TX_STATUS = " + TransactionStatusEnum.TRIED.getCode() + " AND TX_TYPE = " + TransactionTypeEnum.TCC.getCode());
				builderOuter.append(" UNION ALL ").append(builder).append("AND NEXT_RETRY_TIME IS NULL AND GMT_MODIFIED <= ?")
						.append(" AND TX_STATUS = " + TransactionStatusEnum.TRIED.getCode() + " AND TX_TYPE = " + TransactionTypeEnum.TCC.getCode());
			}
			builderOuter.append(") TX ORDER BY PARENT_ID, TX_ID");

			Timestamp gmtCreated = new Timestamp(date.getTime());
			Timestamp nowTime = new Timestamp(System.currentTimeMillis());
			Timestamp timeoutTime = DateUtil.getPrevSecTimestamp(beginTimeoutSecond);

			stmt = conn.prepareStatement(builderOuter.toString());
			stmt.setTimestamp(1, gmtCreated);
			stmt.setString(2, moduleId);
            stmt.setTimestamp(3, nowTime);
			stmt.setTimestamp(4, gmtCreated);
			stmt.setString(5, moduleId);
			stmt.setTimestamp(6, gmtCreated);
			stmt.setString(7, moduleId);
			stmt.setTimestamp(8, nowTime);
			stmt.setTimestamp(9, gmtCreated);
			stmt.setString(10, moduleId);
			stmt.setTimestamp(11, timeoutTime);

			ResultSet resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				transactionInfos.add(resultSet2Bean(resultSet));
			}
		} catch (Throwable e) {
			throw new DistributedTransactionException(e);
		} finally {
			closeStatement(stmt);
			releaseConnection(conn);
		}

		return transactionInfos;
	}

	@Override
	protected List<TransactionInfo> doFind(TransactionInfo transactionInfo, boolean isLock) {
		Connection conn= DataSourceUtils.getConnection(dataSourceAdaptor.getDataSource());

		PreparedStatement stmt = null;

		List<TransactionInfo> transactionInfos = new ArrayList<TransactionInfo>();

		try {

			StringBuilder builder = new StringBuilder();
			builder.append(SELECT_DQL + " FROM TRANSACTION_INFO WHERE 1=1 ");
			if (transactionInfo.getTxId() > -1L) {
				builder.append("AND TX_ID = ? ");
			}
			if (transactionInfo.getParentId() > -1L) {
				builder.append("AND PARENT_ID = ? ");
			}
			if (!Strings.isNullOrEmpty(transactionInfo.getModuleId())) {
				builder.append("AND MODULE_ID = ? ");
			}
			if (!Strings.isNullOrEmpty(transactionInfo.getBusinessId())) {
				builder.append("AND BUSINESS_ID = ? ");
			}
			if (!Strings.isNullOrEmpty(transactionInfo.getBusinessType())) {
				builder.append("AND BUSINESS_TYPE = ? ");
			}
			if (transactionInfo.getTxType() > -1) {
				builder.append("AND TX_TYPE = ? ");
			}
			if (transactionInfo.getTxStatus() > -1) {
				builder.append("AND TX_STATUS = ? ");
			}
			if (transactionInfo.getGmtCreated() != null) {
				builder.append("AND GMT_CREATED = ? ");
			}
			if (isLock) {
				builder.append(" FOR UPDATE ");
			}
			stmt = conn.prepareStatement(builder.toString());

			int condition = 0;

			if (transactionInfo.getTxId() > -1L) {
				stmt.setLong(++condition, transactionInfo.getTxId());
			}
			if (transactionInfo.getParentId() > -1L) {
				stmt.setLong(++condition, transactionInfo.getParentId());
			}
			if (!Strings.isNullOrEmpty(transactionInfo.getModuleId())) {
				stmt.setString(++condition, transactionInfo.getModuleId());
			}
			if (!Strings.isNullOrEmpty(transactionInfo.getBusinessId())) {
				stmt.setString(++condition, transactionInfo.getBusinessId());
			}
			if (!Strings.isNullOrEmpty(transactionInfo.getBusinessType())) {
				stmt.setString(++condition, transactionInfo.getBusinessType());
			}
			if (transactionInfo.getTxType() > -1) {
				stmt.setInt(++condition, transactionInfo.getTxType());
			}
			if (transactionInfo.getTxStatus() > -1) {
				stmt.setInt(++condition, transactionInfo.getTxStatus());
			}
			if (transactionInfo.getGmtCreated() != null) {
				stmt.setTimestamp(++condition, new Timestamp(transactionInfo.getGmtCreated().getTime()));
			}

			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) {
				transactionInfos.add(resultSet2Bean(resultSet));
			}

		} catch (Throwable e) {
			throw new DistributedTransactionException(e);
		} finally {
			closeStatement(stmt);
			releaseConnection(conn);
		}
		return transactionInfos;
	}

	@Override
	protected TransactionInfo doFindById(long txId) {

		Connection conn= DataSourceUtils.getConnection(dataSourceAdaptor.getDataSource());

		TransactionInfo transactionInfo = null;

		PreparedStatement stmt = null;

		try {

			StringBuilder builder = new StringBuilder();
			builder.append(SELECT_DQL + "  FROM TRANSACTION_INFO WHERE TX_ID = ?");

			stmt = conn.prepareStatement(builder.toString());
			stmt.setLong(1, txId);
			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) {
				transactionInfo = resultSet2Bean(resultSet);
			}
		} catch (Throwable e) {
			throw new DistributedTransactionException(e);
		} finally {
			closeStatement(stmt);
			releaseConnection(conn);
		}

		return transactionInfo;
	}

	protected List<TransactionInfo> doLockByModules(long parentId, List<String> modules) {

		Connection conn= DataSourceUtils.getConnection(dataSourceAdaptor.getDataSource());

		List<TransactionInfo> transactionInfos = new ArrayList<TransactionInfo>();

		PreparedStatement stmt = null;

		try {
			StringBuilder builder = new StringBuilder();
			builder.append(SELECT_DQL + "  FROM TRANSACTION_INFO WHERE PARENT_ID = ? AND TX_ID <> 0 AND MODULE_ID ");

			if (modules.size() > 1) {
				builder.append(" IN (");
				for (int i = 0; i < modules.size(); i++) {
					if (i == 0) {
						builder.append("'" + modules.get(i) + "'");
					} else {
						builder.append(",").append("'" + modules.get(i) + "'");
					}
				}
				builder.append(")");
			} else if (modules.size() == 1) {
				builder.append(" = '" + modules.get(0) + "'");
			}
			builder.append(" AND TX_STATUS NOT IN(")
					.append(TransactionStatusEnum.CANCELLED.getCode()).append(", ")
					.append(TransactionStatusEnum.CONFIRMED.getCode()).append(", ")
					.append(TransactionStatusEnum.MANUAL_CANCEL_WAIT.getCode()).append(", ")
					.append(TransactionStatusEnum.MANUAL_CONFIRM_WAIT.getCode())
					.append(")");
			builder.append(" FOR UPDATE ");

			stmt = conn.prepareStatement(builder.toString());
			stmt.setLong(1, parentId);

			ResultSet resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				transactionInfos.add(resultSet2Bean(resultSet));
			}

		} catch (Throwable e) {
			throw new DistributedTransactionException(e);
		} finally {
			closeStatement(stmt);
			releaseConnection(conn);
		}

		return transactionInfos;
	}

	private TransactionInfo resultSet2Bean(ResultSet resultSet) throws Throwable {
		TransactionInfo transactionInfo = new TransactionInfo();

		transactionInfo.setTxId(resultSet.getLong(1));
		transactionInfo.setParentId(resultSet.getLong(2));
		transactionInfo.setModuleId(resultSet.getString(3));
		transactionInfo.setBusinessId(resultSet.getString(4));
		transactionInfo.setBusinessType(resultSet.getString(5));
		transactionInfo.setTxType(resultSet.getInt(6));
		transactionInfo.setTxStatus(resultSet.getInt(7));
		transactionInfo.setContext(resultSet.getString(8));
		transactionInfo.setRetriedCount(resultSet.getString(9));
		if (resultSet.getTimestamp(10) != null) {
			transactionInfo.setNextRetryTime(new Date(resultSet.getTimestamp(10).getTime()));
		}
		if (resultSet.getTimestamp(11) != null) {
			transactionInfo.setGmtCreated(new Date(resultSet.getTimestamp(11).getTime()));
		}
		if (resultSet.getTimestamp(12) != null) {
			transactionInfo.setGmtModified(new Date(resultSet.getTimestamp(12).getTime()));
		}

		return transactionInfo;
	}

	protected void releaseConnection(Connection conn) {
		DataSourceUtils.releaseConnection(conn, dataSourceAdaptor.getDataSource());
	}

	private void closeStatement(Statement stmt) {
		try {
			JdbcUtils.closeStatement(stmt);
			stmt = null;
		} catch (Exception ex) {
			//throw new DistributedTransactionException(ex);
		}
	}

	private String getDateSql(Connection conn) throws Throwable{
		String databaseName = getDatabaseName(conn).toLowerCase();
		if (databaseName.equals(Constants.ORACLE.toLowerCase())) {
			return ORACLE_DATE_SQL;
		} else if (databaseName.equals(Constants.POSTGRESQL.toLowerCase())){
			return PG_DATE_SQL;
		}else if(databaseName.equals(Constants.MYSQL.toLowerCase())){
			return MYSQL_DATE_SQL;
		}
		throw new Exception("Not support database : " + databaseName);
	}

	private String getDateSubtSecSql(Connection conn, int second) throws Throwable{
		String databaseName = getDatabaseName(conn).toLowerCase();
		if (databaseName.equals(Constants.ORACLE.toLowerCase())) {
			return "(sysdate - " + second + "/(24*60*60))";
		} else if (databaseName.equals(Constants.POSTGRESQL.toLowerCase())){
			return "(now() - interval '"+ second +"sec')";
		}
		throw new Exception("Not support database : " + databaseName);
	}

	private String getLimitSql(Connection conn, int num) throws Throwable{
		String databaseName = getDatabaseName(conn).toLowerCase();
		if (databaseName.equals(Constants.ORACLE.toLowerCase())) {
			return " ROWNUM <= " + num;
		} else if (databaseName.equals(Constants.POSTGRESQL.toLowerCase())){
			return " LIMIT " + num;
		}
		throw new Exception("Not support database : " + databaseName);
	}

	private String getDatabaseName(Connection conn) throws Throwable{
		return conn.getMetaData().getDatabaseProductName();
	}


	@Override
	public List<TransactionInfo> listSince(Date date) {

		Connection conn= DataSourceUtils.getConnection(dataSourceAdaptor.getDataSource());

		List<TransactionInfo> transactionInfos = new ArrayList<TransactionInfo>();

		PreparedStatement stmt = null;

		try {

			StringBuilder builder = new StringBuilder();
			builder.append(SELECT_DQL + " FROM TRANSACTION_INFO WHERE GMT_MODIFIED > ?");

			stmt = conn.prepareStatement(builder.toString());

			stmt.setTimestamp(1, new Timestamp(date.getTime()));

			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) {
				transactionInfos.add(resultSet2Bean(resultSet));
			}
		} catch (Throwable e) {
			throw new DistributedTransactionException(e);
		} finally {
			closeStatement(stmt);
			releaseConnection(conn);
		}

		return transactionInfos;
	}
}
