package com.gearrent.dao;

import com.gearrent.entity.Branch;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BranchDAO {

	private final Connection conn;

	public BranchDAO() throws Exception {
		conn = DBConnection.getConnection();
	}

	public List<Branch> getAll() throws SQLException {
		List<Branch> branches = new ArrayList<>();
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM branch ORDER BY name");
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			branches.add(new Branch(
				rs.getInt("branch_id"),
				rs.getString("branch_code"),
				rs.getString("name"),
				rs.getString("address"),
				rs.getString("contact")
			));
		}
		return branches;
	}

	public Branch getById(int branchId) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM branch WHERE branch_id=?");
		ps.setInt(1, branchId);
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			return new Branch(
				rs.getInt("branch_id"),
				rs.getString("branch_code"),
				rs.getString("name"),
				rs.getString("address"),
				rs.getString("contact")
			);
		}
		return null;
	}

	public Branch getByCode(String branchCode) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM branch WHERE branch_code=?");
		ps.setString(1, branchCode);
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			return new Branch(
				rs.getInt("branch_id"),
				rs.getString("branch_code"),
				rs.getString("name"),
				rs.getString("address"),
				rs.getString("contact")
			);
		}
		return null;
	}

	public void save(Branch branch) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(
			"INSERT INTO branch (branch_code, name, address, contact) VALUES (?, ?, ?, ?)");
		ps.setString(1, branch.getBranchCode());
		ps.setString(2, branch.getName());
		ps.setString(3, branch.getAddress());
		ps.setString(4, branch.getContact());
		ps.executeUpdate();
	}

	public void update(Branch branch) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(
			"UPDATE branch SET branch_code=?, name=?, address=?, contact=? WHERE branch_id=?");
		ps.setString(1, branch.getBranchCode());
		ps.setString(2, branch.getName());
		ps.setString(3, branch.getAddress());
		ps.setString(4, branch.getContact());
		ps.setInt(5, branch.getBranchId());
		ps.executeUpdate();
	}

	public void delete(int branchId) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("DELETE FROM branch WHERE branch_id=?");
		ps.setInt(1, branchId);
		ps.executeUpdate();
	}
}
