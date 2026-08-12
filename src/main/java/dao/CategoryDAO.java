package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import model.Category;
import util.DBConnection;

public class CategoryDAO {

    // ================= Add Category =================

    public boolean addCategory(Category category) {

        boolean status = false;

        try {

            Connection con = DBConnection.getConnection();

            String sql = "INSERT INTO categories(user_id, category_name, type) VALUES(?,?,?)";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, category.getUserId());
            ps.setString(2, category.getCategoryName());
            ps.setString(3, category.getType());

            status = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return status;
    }

    // ================= View All Categories =================

    public ArrayList<Category> getCategories(int userId) {

        ArrayList<Category> list = new ArrayList<>();

        try {

            Connection con = DBConnection.getConnection();

            String sql =
                    "SELECT * FROM categories WHERE user_id=? ORDER BY category_id DESC";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Category c = new Category();

                c.setCategoryId(rs.getInt("category_id"));
                c.setUserId(rs.getInt("user_id"));
                c.setCategoryName(rs.getString("category_name"));
                c.setType(rs.getString("type"));

                list.add(c);
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return list;
    }

    // ================= View Categories By Type =================

    public ArrayList<Category> getCategoriesByType(int userId, String type) {

        ArrayList<Category> list = new ArrayList<>();

        try {

            Connection con = DBConnection.getConnection();

            String sql =
                    "SELECT * FROM categories WHERE user_id=? AND type=? ORDER BY category_name";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, userId);
            ps.setString(2, type);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Category c = new Category();

                c.setCategoryId(rs.getInt("category_id"));
                c.setUserId(rs.getInt("user_id"));
                c.setCategoryName(rs.getString("category_name"));
                c.setType(rs.getString("type"));

                list.add(c);
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return list;
    }

    // ================= Get Category By ID =================

    public Category getCategoryById(int id) {

        Category c = null;

        try {

            Connection con = DBConnection.getConnection();

            String sql =
                    "SELECT * FROM categories WHERE category_id=?";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                c = new Category();

                c.setCategoryId(rs.getInt("category_id"));
                c.setUserId(rs.getInt("user_id"));
                c.setCategoryName(rs.getString("category_name"));
                c.setType(rs.getString("type"));
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return c;
    }

    // ================= Update Category =================

    public boolean updateCategory(Category c) {

        boolean status = false;

        try {

            Connection con = DBConnection.getConnection();

            String sql =
                    "UPDATE categories SET category_name=?, type=? WHERE category_id=?";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, c.getCategoryName());
            ps.setString(2, c.getType());
            ps.setInt(3, c.getCategoryId());

            status = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return status;
    }

    // ================= Delete Category =================

    public boolean deleteCategory(int id) {

        boolean status = false;

        try {

            Connection con = DBConnection.getConnection();

            String sql =
                    "DELETE FROM categories WHERE category_id=?";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, id);

            status = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return status;
    }

}