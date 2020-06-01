package com.app.kopbi.dao.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.app.kopbi.dao.TransaksiKasDAO;
import com.app.kopbi.model.ParamTransaksiKas;
import com.app.kopbi.model.ResponseData;
import com.app.kopbi.model.ReportTransaksiKas;
import com.app.kopbi.model.ReportTransaksiKasValid;

@Repository
public class TransaksiKasDAOImpl implements TransaksiKasDAO {

	@Autowired
	MongoTemplate template;

	@Override
	public ResponseData list(Map<String, Object> filter) {
		
		FilterQuery fil = new FilterQuery(filter);
		Query query = fil.getFilter();
//		if (filter.getTanggalMulai().trim().length() > 0 && filter.getTanggalAkhir().trim().length() > 0) {
//			query.addCriteria(new Criteria().andOperator(
//					Criteria.where("tglTransaksi").gte(filter.getTanggalMulai()).lt(filter.getTanggalAkhir()),
//					Criteria.where("dariKas").regex(filter.getDariKas()),
//					Criteria.where("untukKas").regex(filter.getUntukKas()),
//					Criteria.where("jenisTransaksi").regex(filter.getJenisTransaksi()),
//					Criteria.where("statusAktif").is("AKTIF")));
//		} else {
//			query.addCriteria(new Criteria().andOperator(Criteria.where("dariKas").regex(filter.getDariKas()),
//					Criteria.where("untukKas").regex(filter.getUntukKas()),
//					Criteria.where("jenisTransaksi").regex(filter.getJenisTransaksi()),
//					Criteria.where("statusAktif").is("AKTIF")));
//		}
		Long count = getCount(query);
		int mulai = 0;
		int akhir = 0;
		try {
			mulai = Double.valueOf(filter.get("mulai").toString()).intValue();
			mulai = mulai-1;
			akhir = Double.valueOf(filter.get("akhir").toString()).intValue();
		}catch(Exception e) {
			mulai = 0;
			akhir = 0;
		}
		query.skip((mulai));
		query.limit(akhir);
		ResponseData data = new ResponseData();
		data.setAkhir(akhir);
		data.setMulai(mulai);
		data.setTotalRow(count);
		data.setData(template.find(query, ReportTransaksiKas.class));
		return data;
	}

	private Long getCount(Query query) {
		return template.count(query, ReportTransaksiKas.class);
	}

	@Override
	public boolean saveTransaksiKas(ReportTransaksiKas kas) {
		try {
			if (kas.getJenisTransaksi().trim().length() > 1) {
				template.save(kas);
				if (kas.getJenisTransaksi().equalsIgnoreCase("Transfer")) {
					return transTransfer(kas);
				} else {
					return transNotTransfer(kas);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean transTransfer(ReportTransaksiKas kas) {
		Query query = new Query();
		query.addCriteria(Criteria.where("kodeKas").is(kas.getKodeUntukKas()));
		ReportTransaksiKasValid find = (ReportTransaksiKasValid) template.findOne(query, ReportTransaksiKasValid.class);
		Double saldoAkhir = 0d;
		if (find == null) {
			find = new ReportTransaksiKasValid();
			find.setKeterangan(kas.getJenisTransaksi());
			find.setKetKas(kas.getUntukKas());
			find.setKodeKas(kas.getKodeUntukKas());
			find.setNominal(kas.getNominal());

		} else {
			try {
				saldoAkhir = Double.valueOf(find.getNominal());
			} catch (Exception e) {
				saldoAkhir = 0d;
			}
			Double saldoUpdt = Double.valueOf(kas.getNominal());
			saldoAkhir = saldoAkhir + saldoUpdt;
			find.setNominal(String.valueOf(saldoAkhir));
		}

		query = new Query();
		query.addCriteria(Criteria.where("kodeKas").is(kas.getKodeDariKas()));
		ReportTransaksiKasValid findTransfer = (ReportTransaksiKasValid) template.findOne(query,
				ReportTransaksiKasValid.class);
		try {
			saldoAkhir = Double.valueOf(findTransfer.getNominal());
		} catch (Exception e) {
			saldoAkhir = 0d;
		}
		Double saldoUpdt = Double.valueOf(kas.getNominal());
		saldoAkhir = saldoAkhir - saldoUpdt;
		findTransfer.setNominal(String.valueOf(saldoAkhir));
		findTransfer.setTanggalUpdate(kas.getTanggalUpdate());
		findTransfer.setUserUpdate(kas.getUserUpdate());
		find.setTanggalUpdate(kas.getTanggalUpdate());
		find.setUserUpdate(kas.getUserUpdate());
		try {
			template.save(find);
			template.save(findTransfer);
			return true;
		} catch (Exception e) {

		}
		return false;
	}

	private boolean transNotTransfer(ReportTransaksiKas kas) {
		Query query = new Query();
		Double saldoAkhirCalc = 0d;
		if (kas.getJenisTransaksi().trim().length() > 1) {

			query = new Query();

			if (kas.getJenisTransaksi().equalsIgnoreCase("Setoran") || kas.getJenisTransaksi().equalsIgnoreCase("Pemasukan")
					 || kas.getJenisTransaksi().equalsIgnoreCase("Angsuran")) {
				query.addCriteria(Criteria.where("kodeKas").is(kas.getKodeUntukKas()));
			}

			if (kas.getJenisTransaksi().equalsIgnoreCase("Pinjaman")
					|| kas.getJenisTransaksi().equalsIgnoreCase("Pengeluaran")
					|| kas.getJenisTransaksi().equalsIgnoreCase("Penarikan")) {
				query.addCriteria(Criteria.where("kodeKas").is(kas.getKodeDariKas()));
			}

			ReportTransaksiKasValid find = (ReportTransaksiKasValid) template.findOne(query,
					ReportTransaksiKasValid.class);
			Double saldoAkhir = 0d;
			if (find == null) {
				find = new ReportTransaksiKasValid();
				find.setKeterangan(kas.getJenisTransaksi());
				find.setKetKas(kas.getUntukKas());
				find.setKodeKas(kas.getKodeUntukKas());
				if (kas.getJenisTransaksi().equalsIgnoreCase("Pinjaman")
						|| kas.getJenisTransaksi().equalsIgnoreCase("Pengeluaran")
						|| kas.getJenisTransaksi().equalsIgnoreCase("Penarikan")) {
					find.setKetKas(kas.getDariKas());
					find.setKodeKas(kas.getKodeDariKas());
				}
				find.setNominal(kas.getNominal());
				find.setTanggalUpdate(kas.getTanggalUpdate());
				find.setUserUpdate(kas.getUserUpdate());
				template.save(find);
				System.out.println("DATA UPDATE REPORT 0 : " + find.getNominal());
				return true;
			}
			try {
				saldoAkhir = Double.valueOf(find.getNominal());
			} catch (Exception e) {
				saldoAkhir = 0d;
			}
			if (kas.getKetJenisTransaksi() == null || !kas.getKetJenisTransaksi().equalsIgnoreCase("PERUBAHAN")) {
				Double saldoUpdt = Double.valueOf(kas.getNominal());
				if (kas.getKetKas().equalsIgnoreCase("D")) {
					saldoAkhir = saldoAkhir + saldoUpdt;
				}
				if (kas.getKetKas().equalsIgnoreCase("K")) {
					saldoAkhir = saldoAkhir - saldoUpdt;
				}
				saldoAkhirCalc = saldoAkhir;
				find.setNominal(String.valueOf(saldoAkhir));
				find.setTanggalUpdate(kas.getTanggalUpdate());
				find.setUserUpdate(kas.getUserUpdate());
				System.out.println("DATA UPDATE REPORT 1 : " + find.getNominal());
				template.save(find);
				return true;
			} else {
				final ReportTransaksiKasValid rtv = find;
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Query qy = new Query();
						qy.addCriteria(Criteria.where("kodeUntukKas").is(kas.getKodeUntukKas()));
						List<ReportTransaksiKas> list = template.find(qy, ReportTransaksiKas.class);
						Double nominal = 0d;
						for (ReportTransaksiKas k : list) {
							if (k.getKetKas().equalsIgnoreCase("D")) {
								nominal = nominal + Double.valueOf(k.getNominal());
							}
							if (k.getKetKas().equalsIgnoreCase("K")) {
								nominal = nominal - Double.valueOf(k.getNominal());
							}
						}
						rtv.setNominal(String.valueOf(nominal));
						System.out.println("DATA UPDATE REPORT 2 : " + rtv.getNominal());
						template.save(rtv);
					}
				}).start();

			}
			return true;
		}
		return false;
	}

	@Override
	public boolean importFile(String split) {
		try {
			List<ReportTransaksiKas> list = getList(split);
			template.insert(list, ReportTransaksiKas.class);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private List<ReportTransaksiKas> getList(String split) {
		List<ReportTransaksiKas> list = new ArrayList();
		String csvFile = "/home/mti/projectkopbi/data_kas.csv";
		BufferedReader br = null;
		String line = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		try {
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
				String[] a = line.split(split);
				ReportTransaksiKas t = new ReportTransaksiKas();
				try {
					t.setAkun(a[4].trim());
				} catch (Exception e) {
					t.setAkun("");
				}
				try {
					t.setDariKas(a[5].trim());
				} catch (Exception e) {
					t.setDariKas("");

				}
				try {
					t.setJenisTransaksi(a[7].trim());
				} catch (Exception e) {
					t.setJenisTransaksi("");
				}
				try {
					t.setKeterangan(a[3].trim());
				} catch (Exception e) {
					t.setKeterangan("");
				}
				try {
					t.setKetJenisTransaksi("");
				} catch (Exception e) {
					t.setKetJenisTransaksi("");
				}
				try {
					t.setKetKas(a[8].trim());
				} catch (Exception e) {
					t.setKetKas("");
				}
				try {
					t.setNominal(a[2].trim());
				} catch (Exception e) {
					t.setNominal("");
				}
				try {
					t.setTanggalTransaksi(a[1].trim());
				} catch (Exception e) {
					t.setTanggalTransaksi("");
				}
				try {
					t.setTanggalUpdate(sdf.format(new Date()));
				} catch (Exception e) {
					t.setTanggalUpdate("");
				}
				try {
					t.setUntukKas(a[6].trim());
				} catch (Exception e) {
					t.setUntukKas("");
				}
				try {
					t.setUserUpdate(a[10].trim());
				} catch (Exception e) {
					t.setUserUpdate("");
				}
				list.add(t);
			}
		} catch (Exception e) {
		}
		return list;
	}

	@Override
	public ResponseData listRekap() {
		Query query = new Query();
		ResponseData data = new ResponseData();
		data.setAkhir(1);
		data.setMulai(100);
		data.setTotalRow(100l);
		data.setData(template.find(query, ReportTransaksiKasValid.class));
		return data;
	}
}
