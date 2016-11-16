package io.anyway.galaxy.infoBoard;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Response;

import com.alibaba.fastjson.JSON;

import io.anyway.galaxy.domain.TransactionInfo;
import io.anyway.galaxy.repository.impl.JdbcTransactionRepository;
import io.anyway.galaxy.util.DateUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		JdbcTransactionRepository transactionRepository = new JdbcTransactionRepository();

		String param = req.getParameter("interval");
		int interval = 0;

		if(StringUtils.isNotEmpty(param)){
			interval = Integer.parseInt(param);
		}

		List<TransactionInfo> list = transactionRepository.listSince(DateUtil.getPrevDate(interval));

		String jsonStr = JSON.toJSONString(list);
		resp.setCharacterEncoding("utf-8");
		resp.addHeader("Access-Control-Allow-Origin", "*");
		resp.addHeader("Access-Control-Allow-Headers","Content-Type, Accept");
		resp.setContentType("application/json");

		PrintWriter writer = resp.getWriter();
		try{
			writer.write(jsonStr);
			resp.setStatus(Response.SC_OK);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
			

	}

}
