package com.app.kopbi.model;

import org.springframework.data.annotation.Id;
import org.springframework.stereotype.Component;

@Component
public class ReportTransaksiKasValid {

	@Id
	String id;
	String nominal;
	String keterangan;
	String kodeKas;
	String ketKas;
	String tanggalUpdate;
	String userUpdate;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getNominal() {
		return nominal;
	}
	public void setNominal(String nominal) {
		this.nominal = nominal;
	}
	public String getKeterangan() {
		return keterangan;
	}
	public void setKeterangan(String keterangan) {
		this.keterangan = keterangan;
	}
	public String getKodeKas() {
		return kodeKas;
	}
	public void setKodeKas(String kodeKas) {
		this.kodeKas = kodeKas;
	}
	public String getKetKas() {
		return ketKas;
	}
	public void setKetKas(String ketKas) {
		this.ketKas = ketKas;
	}
	public String getTanggalUpdate() {
		return tanggalUpdate;
	}
	public void setTanggalUpdate(String tanggalUpdate) {
		this.tanggalUpdate = tanggalUpdate;
	}
	public String getUserUpdate() {
		return userUpdate;
	}
	public void setUserUpdate(String userUpdate) {
		this.userUpdate = userUpdate;
	}
	
}
