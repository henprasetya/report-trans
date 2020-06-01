package com.app.kopbi.dao;

import java.util.Map;

import com.app.kopbi.model.ResponseData;
import com.app.kopbi.model.ReportTransaksiKas;

public interface TransaksiKasDAO {

	ResponseData list(Map<String, Object> transaksiKas);
	boolean saveTransaksiKas(ReportTransaksiKas kas);
	boolean importFile(String split);
	ResponseData listRekap();
}
