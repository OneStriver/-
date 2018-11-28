package com.fh.util.querymenu;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.fh.entity.consts.RegisterMenuModuleInfo;
import com.fh.entity.system.Menu;

public class DataBaseUtil {

	//查询sys_menu_all所有的父节点
	public static Integer countAllParentMenu(RegisterMenuModuleInfo registerMenuModuleInfo){
		String sql = "select count(*) as menuCount "
				+ " from " + registerMenuModuleInfo.getDatabaseName() + ".sys_menu_all where PARENT_ID=0 ";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getEachModuleConnection(registerMenuModuleInfo);
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				Integer menuCount = Integer.valueOf(rs.getString("menuCount"));
				return menuCount;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			free(rs, ps, conn);
		}
		return 0;
	}
	
	//查询sys_menu_all所有的子节点
	public static Integer countAllSubMenu(RegisterMenuModuleInfo registerMenuModuleInfo){
		String sql = "select count(*) as menuCount "
				+ " from " + registerMenuModuleInfo.getDatabaseName() + ".sys_menu_all where PARENT_ID!=0 ";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getEachModuleConnection(registerMenuModuleInfo);
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				Integer menuCount = Integer.valueOf(rs.getString("menuCount"));
				return menuCount;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			free(rs, ps, conn);
		}
		return 0;
	}
	
	//根据ID查询所有的菜单表
	public static List<Menu> selectModuleMenuInfoByParentId(RegisterMenuModuleInfo registerMenuModuleInfo,Integer parentId){
		List<Menu> cacheMenuList = new ArrayList<Menu>();
		String sql = "select MENU_ID,MENU_NAME,MENU_URL,PARENT_ID,MENU_ORDER,MENU_ICON,MENU_TYPE,MENU_STATE "
				+ " from " + registerMenuModuleInfo.getDatabaseName() + ".sys_menu where PARENT_ID=" + parentId;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getEachModuleConnection(registerMenuModuleInfo);
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				Menu menu = new Menu();
				menu.setMENU_ID(Integer.valueOf(rs.getString("MENU_ID")));
				menu.setMENU_NAME(rs.getString("MENU_NAME"));
				String projectName = registerMenuModuleInfo.getProjectName();
				String menuUrlStr = rs.getString("MENU_URL");
				if(!"#".equals(menuUrlStr)){
					menu.setMENU_URL("../"+projectName+"/"+menuUrlStr);
				}else{
					menu.setMENU_URL(menuUrlStr);
				}
				menu.setPARENT_ID(Integer.valueOf(rs.getString("PARENT_ID")));
				menu.setMENU_ORDER(rs.getString("MENU_ORDER"));
				menu.setMENU_ICON(rs.getString("MENU_ICON"));
				menu.setMENU_TYPE(rs.getString("MENU_TYPE"));
				menu.setMENU_STATE(rs.getString("MENU_STATE"));
				cacheMenuList.add(menu);
			}
			return cacheMenuList;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			free(rs, ps, conn);
		}
		return null;
	}

	//写入数据库对应的菜单
	public static void insertModuleMenuInfo(Menu menu,RegisterMenuModuleInfo registerMenuModuleInfo) {
		String sql = "insert into "+registerMenuModuleInfo.getDatabaseName()+".sys_menu_all(MENU_ID,MENU_NAME,MENU_URL,PARENT_ID,MENU_ORDER,MENU_ICON,MENU_TYPE,MENU_STATE) values(?,?,?,?,?,?,?,?) ";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getEachModuleConnection(registerMenuModuleInfo);
			ps = conn.prepareStatement(sql);
			ps.setInt(1, menu.getMENU_ID());
			ps.setString(2, menu.getMENU_NAME());
			ps.setString(3, menu.getMENU_URL());
			ps.setInt(4, menu.getPARENT_ID());
			ps.setString(5, menu.getMENU_ORDER());
			ps.setString(6, menu.getMENU_ICON());
			ps.setString(7, menu.getMENU_TYPE());
			ps.setString(8, menu.getMENU_STATE());
			ps.execute();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			free(rs, ps, conn);
		}
	}
	
	//清空sys_menu_all表中所有的数据
	public static void deleteAllModuleMenuInfo(RegisterMenuModuleInfo registerMenuModuleInfo) {
		String sql = "delete from " + registerMenuModuleInfo.getDatabaseName() + ".sys_menu_all";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getEachModuleConnection(registerMenuModuleInfo);
			ps = conn.prepareStatement(sql);
			ps.execute();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			free(rs, ps, conn);
		}
	}
	
	private static Connection getEachModuleConnection(RegisterMenuModuleInfo registerMenuModuleInfo) {
		try {
			Connection conn = DriverManager.getConnection(
								registerMenuModuleInfo.getDatabaseUrl(),
								registerMenuModuleInfo.getDatabaseUserName(), 
								registerMenuModuleInfo.getDatabasePassWord());
			return conn;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static void free(ResultSet rs, Statement st, Connection conn) {
		try {
			if (rs != null)
				rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (st != null)
					st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (conn != null)
					try {
						conn.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
			}
		}
	}

}
