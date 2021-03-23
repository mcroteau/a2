package n.jdbc;

import n.annotate.Element;
import n.annotate.Inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Element
public class Q {

    @Inject
    BasicDataSource dataSource;

    public void setDataSource(BasicDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Object get(String preSql, Object[] params, Class cls){
        Object result = null;
        try {
            String sql = hydrateSql(preSql, params);
            Connection connection = dataSource.getConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            if(rs.next()){
                result = extractData(rs, cls);
            }
            if(result == null){
                throw new Exception(cls + " not found using '" + sql + "'");
            }

            connection.commit();
            connection.close();
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return result;
    }

    public boolean save(String preSql, Object[] params){
        try {
            String sql = hydrateSql(preSql, params);
            Connection connection = dataSource.getConnection();
            Statement stmt = connection.createStatement();
            Boolean saved = stmt.execute(sql);
            connection.commit();
            connection.close();
        }catch(Exception ex){
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public List<Object> list(String preSql, Object[] params, Class cls){
        List<Object> results = new ArrayList<>();
        try {
            String sql = hydrateSql(preSql, params);
            Connection connection = dataSource.getConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            results = new ArrayList<>();
            while(rs.next()){
                Object obj = extractData(rs, cls);
                results.add(obj);
            }
            connection.commit();
            connection.close();
        }catch(ClassCastException ccex){
            System.out.println("");
            System.out.println("Wrong Class type, attempted to cast the return data as a " + cls);
            System.out.println("");
            ccex.printStackTrace();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return results;
    }

    public boolean update(String preSql, Object[] params){
        try {
            String sql = hydrateSql(preSql, params);
            Connection connection = dataSource.getConnection();
            Statement stmt = connection.createStatement();
            Boolean rs = stmt.execute(sql);
            connection.commit();
            connection.close();
        }catch(Exception ex){
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean delete(String preSql, Object[] params){
        try {
            String sql = hydrateSql(preSql, params);
            Connection connection = dataSource.getConnection();
            Statement stmt = connection.createStatement();
            boolean rs = stmt.execute(sql);
            connection.commit();
            connection.close();
        }catch(Exception ex){
            ex.printStackTrace();
            return false;
        }
        return true;
    }


    protected String hydrateSql(String sql, Object[] params){
        for(Object object : params){
            sql = sql.replaceFirst("\\{\\}", object.toString());
        }
        return sql;
    }

    protected Object extractData(ResultSet rs, Class cls) throws Exception{
        Object object = new Object();
        Constructor[] constructors = cls.getConstructors();
        for(Constructor constructor: constructors){
            if(constructor.getParameterCount() == 0){
                object = constructor.newInstance();
            }
        }

        Field[] fields = object.getClass().getDeclaredFields();
        for(Field field: fields){
            field.setAccessible(true);
            String originalName = field.getName();
            String regex = "([a-z])([A-Z]+)";
            String replacement = "$1_$2";
            String name = originalName.replaceAll(regex, replacement).toLowerCase();
            Type type = field.getType();
            if (hasColumn(rs, name)) {
                if (type.getTypeName().equals("int")) {
                    field.set(object, rs.getInt(name));
                } else if (type.getTypeName().equals("double")) {
                    field.set(object, rs.getDouble(name));
                } else if (type.getTypeName().equals("long")) {
                    field.set(object, rs.getLong(name));
                } else if (type.getTypeName().equals("java.lang.Integer")) {
                    field.set(object, rs.getInt(name));
                } else if (type.getTypeName().equals("java.lang.Long")) {
                    field.set(object, rs.getLong(name));
                } else if (type.getTypeName().equals("java.math.BigDecimal")) {
                    field.set(object, rs.getBigDecimal(name));
                } else if (type.getTypeName().equals("java.lang.Boolean")) {
                    field.set(object, rs.getBoolean(name));
                } else if (type.getTypeName().equals("java.lang.String")) {
                    field.set(object, rs.getString(name));
                }
            }
        }
        return object;
    }

    public static boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        for (int x = 1; x <= rsmd.getColumnCount(); x++) {
            if (columnName.equals(rsmd.getColumnName(x).toLowerCase())) {
                return true;
            }
        }
        return false;
    }

}
