package com.petshop.dao;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import com.petshop.entity.MapperOrder;
import com.petshop.entity.MapperOrderDetail;
import com.petshop.entity.Order;
import com.petshop.entity.OrderDetail;
import com.petshop.entity.Order.OrderStatus;
import com.mysql.cj.protocol.a.SqlTimestampValueEncoder;
import com.petshop.dao.OrderDetailDao;

@Repository
public class OrderDao extends BaseDao {
	@Autowired
	private OrderDetailDao orderDetailDao;

	public int create(Order order) {
		order.setOrderTime(LocalDateTime.now());
		order.setStatus(OrderStatus.PENDING);
		String sql = "INSERT INTO order_customer VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ,?)";
		Object[] params = { order.getOrderId(), order.getCustomerId(), order.getRecipientName(), order.getPhoneNumber(),
				order.getEmail(), order.getAddress(), order.getOrderTime(), null, null, null,
				order.getStatus().toString(), order.getShippingFee(), order.getTotalPrice() };
		try {

			int rowsInserted = _JdbcTemplate.update(sql, params);
			return rowsInserted;
		}

		catch (Exception e) {
			System.out.println(e);
			return 0;
		}
	}

	public Order findOrder(String OrderID) {
		Order order = new Order();
		try {
			String sql = " SELECT * FROM order_customer WHERE orderID='" + OrderID + "'";
			order = _JdbcTemplate.queryForObject(sql, new MapperOrder(orderDetailDao));
			return order;

		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}

	public List<Order> findAllOrder(String customerID) {
		List<Order> orderList = new ArrayList<>();
		try {
			String sql = " SELECT * FROM order_customer WHERE customerID='" + customerID + "'";
			orderList = _JdbcTemplate.query(sql, new MapperOrder(orderDetailDao));
			return orderList;

		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}

	public int DeleteOrder(String orderID) {
		try {
			String sql = "  UPDATE order_customer SET order_status='CANCELED' WHERE orderID=?";
			Object param = orderID;
			int updatedRow = _JdbcTemplate.update(sql, param);
			return updatedRow;
		} catch (Exception e) {
			System.out.println(e);
			return 0;
		}
	}

	public List<Order> findOrderByStatus(String status, String customerID) {
		try {
			List<Order> orderList = new ArrayList<>();
			String sql = "SELECT * FROM order_customer WHERE order_status=? and customerID=?";
			Object[] param = { status, customerID };
			orderList = _JdbcTemplate.query(sql, new MapperOrder(orderDetailDao), param);
			return orderList;
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}

	public List<Order> GetDataOrder() {
		try {
			List<Order> orderList = new ArrayList<>();
			String sql = "SELECT * FROM order_customer ";
			orderList = _JdbcTemplate.query(sql, new MapperOrder(orderDetailDao));
			return orderList;
		} catch (NullPointerException e) {
			System.out.println(e);
			return null;
		}
	}

	public List<Order> GetDataOrderByStatus(String status, String month, String year)
			throws SQLException, NullPointerException {
		try {
			List<Order> orderList = new ArrayList<>();
			StringBuffer sql = new StringBuffer("SELECT * FROM order_customer");
			Object[] params = null;
			if (!status.equals("all")) {
				sql.append(" WHERE order_status =?");
				sql.append(" AND YEAR(OrderTime)=?");
				if (!month.equals("null")) {
					sql.append(" AND MONTH(OrderTime)=?");
					params = new Object[] { status, Integer.parseInt(year), Integer.parseInt(month) };
				} else
					params = new Object[] { status, Integer.parseInt(year) };
				orderList = _JdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(Order.class), params);
			} else {
				sql.append(" WHERE YEAR(OrderTime)=?");
				if (!month.equals("null")) {
					sql.append(" AND MONTH(OrderTime)=?");
					params = new Object[] { Integer.parseInt(year), Integer.parseInt(month) };
				} else
					params = new Object[] { Integer.parseInt(year) };
				orderList = _JdbcTemplate.query(sql.toString(), new MapperOrder(orderDetailDao),params);
			}

			return orderList;
		} catch (DataAccessException e) {
			System.out.println(e);
			return null;
		}
	}

	public List<Order> GetDataOrderByUsername(String username) {
		try {
			List<Order> orderList = new ArrayList<>();
			String sql = "SELECT * FROM order_customer where customerID ='" + username + "'";
			orderList = _JdbcTemplate.query(sql, new MapperOrder(orderDetailDao));
			return orderList;
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}

	public List<Order> GetDataOrderPaginate(int start, int end, String status,String month,String year) {
		List<Order> listOrder = new ArrayList<>();
		try {
			String sql = SqlOrderPaginate(start, end, status,month,year).toString();
			listOrder = _JdbcTemplate.query(sql, new MapperOrder(orderDetailDao));
			return listOrder;
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}

	public StringBuffer SqlOrderPaginate(int start, int totalPage, String status, String month, String year) {
		StringBuffer sql = new StringBuffer();
		if (status.equals("all")) {
			sql.append("SELECT * FROM order_customer");
			sql.append(" WHERE YEAR(OrderTime)='" + year + "'");
			if (!month.equals("null")) {
				sql.append(" AND MONTH(OrderTime)='" + month + "'");
			}
		} else {
			sql.append("SELECT * FROM order_customer WHERE order_status ='" + status + "'");
			sql.append(" AND YEAR(OrderTime)='" + year + "'");
			if (!month.equals("null")) {
				sql.append(" AND MONTH(OrderTime)='" + month + "'");
			}
		}
		sql.append(" LIMIT ");
		sql.append(start + ", " + totalPage);
		return sql;
	}

	public int UpdateOrder(Order order) {
		try {
			String sql = "UPDATE order_customer SET order_status = ?, address = ?, confirmtime = ? WHERE orderID = ?";
			Object[] param = {
				order.getStatus(),
				order.getAddress(),
				order.getConfirmTime(),
				order.getOrderId()
			};
			int updatedRow = _JdbcTemplate.update(sql, param);
			return updatedRow;
		} catch (Exception e) {
			System.out.println(e);
			return 0;
		}
	}
	public List<String> FindMonthOrderInYear() {
		try {
			String sql = "SELECT DISTINCT(MONTH(orderTime)) FROM order_customer WHERE  YEAR(OrderTime)=2023 ORDER BY MONTH(orderTime) ASC";
			List<String> results = _JdbcTemplate.queryForList(sql, String.class);
			return results;
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}

	public List<Long> FindTotalPriceInMonthAndYear() {
		try {
			String sql = "SELECT SUM(totalPrice) FROM order_customer WHERE  YEAR(OrderTime)=2023 GROUP BY MONTH(orderTime) ORDER BY MONTH(orderTime) ASC";
			List<Long> results = _JdbcTemplate.queryForList(sql, Long.class);
			return results;
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}

	public List<Long> FindTotalOrderInMonthAndYear() {
		try {
			String sql = "SELECT count(orderID) FROM order_customer WHERE  YEAR(OrderTime)=2023 GROUP BY MONTH(orderTime) ORDER BY MONTH(orderTime) ASC";
			List<Long> results = _JdbcTemplate.queryForList(sql, Long.class);
			return results;
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}
}
