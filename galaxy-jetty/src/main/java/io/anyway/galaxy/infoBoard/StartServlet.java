package io.anyway.galaxy.infoBoard;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Response;

import io.anyway.galaxy.domain.TransactionInfo;
import io.anyway.galaxy.repository.impl.JdbcTransactionRepository;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class StartServlet extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		
		JdbcTransactionRepository transactionRepository = new JdbcTransactionRepository();

		String param = req.getParameter("txid");

		long txId = Long.parseLong(param);

		TransactionInfo info = transactionRepository.findById(txId);

		if(info != null){
			info.setGmtModified(new Date(System.currentTimeMillis()));
			transactionRepository.update(info);
		}

		resp.setCharacterEncoding("utf-8");
		resp.addHeader("Access-Control-Allow-Origin", "*");
		resp.addHeader("Access-Control-Allow-Headers","Content-Type, Accept");
		resp.setContentType("application/json");

		PrintWriter writer = resp.getWriter();
		try{
			resp.setStatus(Response.SC_OK);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
			

	}

}
