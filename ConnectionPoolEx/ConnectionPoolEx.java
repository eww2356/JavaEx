package ConnectionPoolEx;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class ConnectionPoolEx {
	private String databaseUrl;
	private String userName;
	private String password;
	private int maxPoolSize = 10;	// 최대 풀 크기
	private int maxWaitTime = 30 * 1000;	// 커넥션 풀에서 사용 기능한 커넥션이 없을 때 커넥션을 최대로 대기하는 시간(초)
	static private int connNum = 0;

	private static final String SQL_VERIFYCONN = "select 1";

	// 미사용 중인 풀
	static Stack<Connection> freePool = new Stack<>();
	// 사용 중인 풀
	static Set<Connection> occupiedPool = new HashSet<>();

	/**
	 * Constructor
	 * 
	 * @param databaseUrl
	 *            The connection url
	 * @param userName
	 *            user name
	 * @param password
	 *            password
	 * @param maxSize
	 *            max size of the connection pool
	 * @param maxWait
	 *            커넥션 풀에서 사용 기능한 커넥션이 없을 때 커넥션을 최대로 대기하는 시간(초)
	 */
	public ConnectionPoolEx(String databaseUrl, String userName,
			String password, int maxSize, int maxWait) {
		this.databaseUrl = databaseUrl;
		this.userName = userName;
		this.password = password;
		this.maxPoolSize = maxSize;
		this.maxWaitTime = maxWait * 1000;
	}

	/**
	 * Get an available connection
	 * 사용 가능한 연결 가져오기
	 * 
	 * @return An available connection
	 * @throws SQLException
	 *             Fail to get an available connection
	 */
	public synchronized Connection getConnection() throws SQLException {
		Connection conn = null;

		if (isFull()) {
			long startWaitTime = System.currentTimeMillis();
			while(true) {
				//System.out.println("[" + Thread.currentThread().getName() + "] 사용하지 않는 데이터베이스 커넥션 객체가 없어 대기함");
				long endWaitTime = System.currentTimeMillis();
				if (maxWaitTime > 0 && (endWaitTime - startWaitTime) >= maxWaitTime) {	// 대기 시간이 초과된 경우
					System.out.println("[" + Thread.currentThread().getName() + "] 대기 시간(" + ((endWaitTime - startWaitTime) / 1000) + "s)이 초과되어 데이터베이스 커넥션 객체 가져오기를 중지함");
					throw new SQLException("The connection pool is full.");
				} else {
					if (!isFull()) {	// 사용가능한 커넥션이 생겼을 경우
						break;
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

		conn = getConnectionFromPool();

		// If there is no free connection, create a new one.
		if (conn == null) {
			conn = createNewConnectionForPool();
		}

		// For Azure Database for MySQL, if there is no action on one connection for some
		// time, the connection is lost. By this, make sure the connection is
		// active. Otherwise reconnect it.
		// 연결에 한동안 작업이 없으면 연결이 끊어짐. 연결이 활성 상태가 아니면 재연결
		conn = makeAvailable(conn);
		return conn;
	}

	/**
	 * Return a connection to the pool
	 * 연결을 풀에 반환하기
	 * 
	 * @param conn
	 *            The connection
	 * @throws SQLException
	 *             When the connection is returned already or it isn't gotten
	 *             from the pool.
	 */
	public synchronized void returnConnection(Connection conn)
			throws SQLException {
		if (conn == null) {
			throw new NullPointerException();
		}
		if (!occupiedPool.remove(conn)) {
			throw new SQLException(
					"The connection is returned already or it isn't for this pool");
		}
		freePool.push(conn);
	}

	/**
	 * Verify if the connection is full.
	 * 연결이 꽉 찼는지 확인하기
	 * 
	 * @return if the connection is full
	 */
	private synchronized boolean isFull() {
		return ((freePool.size() == 0) && (connNum >= maxPoolSize));
	}

	/**
	 * Create a connection for the pool
	 * 풀에 새로운 연결을 만들어 넣어주기
	 * 
	 * @return the new created connection
	 * @throws SQLException
	 *             When fail to create a new connection.
	 */
	private Connection createNewConnectionForPool() throws SQLException {
		Connection conn = createNewConnection();
		connNum++;
		occupiedPool.add(conn);
		return conn;
	}

	/**
	 * Crate a new connection
	 * 새로운 연결 생성하기
	 * 
	 * @return the new created connection
	 * @throws SQLException
	 *             When fail to create a new connection.
	 */
	private Connection createNewConnection() throws SQLException {
		Connection conn = null;
		conn = DriverManager.getConnection(databaseUrl, userName, password);
		return conn;
	}

	/**
	 * Get a connection from the pool. If there is no free connection, return null
	 * 풀에 있는 연결 가져오기, 만약 미사용 중인 연결이 없다면 null을 반환
	 * 
	 * @return the connection.
	 */
	private Connection getConnectionFromPool() {
		Connection conn = null;
		if (freePool.size() > 0) {
			conn = freePool.pop();
			occupiedPool.add(conn);
		}
		return conn;
	}

	/**
	 * Make sure the connection is available now. Otherwise, reconnect it.
	 * 연결 활성화 하기
	 * 
	 * @param conn
	 *            The connection for verification.
	 * @return the available connection.
	 * @throws SQLException
	 *             Fail to get an available connection
	 */
	private Connection makeAvailable(Connection conn) throws SQLException {
		if (isConnectionAvailable(conn)) {
			return conn;
		}

		// If the connection is't available, reconnect it.
		occupiedPool.remove(conn);
		connNum--;
		conn.close();

		conn = createNewConnection();
		occupiedPool.add(conn);
		connNum++;
		return conn;
	}

	/**
	 * By running a sql to verify if the connection is available
	 * 연결이 사용 가능한지 확인하기 위해 sql 실행해보기
	 * 
	 * @param conn
	 *            The connection for verification
	 * @return if the connection is available for now.
	 */
	private boolean isConnectionAvailable(Connection conn) {
		try (Statement st = conn.createStatement()) {
			st.executeQuery(SQL_VERIFYCONN);
			return true;
		} catch (SQLException e) {
			return false;
		}
	}
	
	/**
	 * 모든 연결들을 닫기
	 * 
	 * @throws SQLException 
	 */
	public static void closeConnections() throws SQLException {
		// 미사용 중인 연결 목록에서 연결 삭제하기
		for (Iterator<Connection> iterator = freePool.iterator(); iterator.hasNext();){
			Connection conn = (Connection) iterator.next();
			conn.close();
			iterator.remove();
		}
		// 사용 중인 연결 목록에서 연결 삭제하기
		for (Iterator<Connection> iterator = occupiedPool.iterator(); iterator.hasNext();){
			Connection conn = (Connection) iterator.next();
			connNum--;
			conn.close();
			iterator.remove();
		}
	}
	
	/**
	 * Connection Pool 활용 예제
	 * 
	 * 최대 2개의 연결을 가질 수 있는 커넥션 풀 객체를 생성하고
	 * 하나의 연결을 가져와서 사용 후 반환하기 
	 */
	public static void main(String[] args) throws SQLException {
		Connection conn = null;
		// 커넥션 풀 객체 생성
		ConnectionPoolEx pool = new ConnectionPoolEx(
				"jdbc:mariadb://(주소):(포트)/(DB명)",
				"(사용자 ID)", "(사용자 비밀번호)", 2, 30);
		
		try {
			// 연결 가져오기
			conn = pool.getConnection();
			// 가져온 연결 사용하기
			try (Statement statement = conn.createStatement())
			{
				ResultSet res = statement.executeQuery("show tables");
				System.out.println("There are below tables:");
				while (res.next()) {
					String tblName = res.getString(1);
					System.out.println(tblName);
				}
				
				// 연결에서 파생된 객체 닫기
				if(res != null) res.close();
				if(statement != null) statement.close();
			}
			// 연결 놓아주기
			if (conn != null) {
				pool.returnConnection(conn);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 모든 연결 닫기
			closeConnections();
		}
	}

}
