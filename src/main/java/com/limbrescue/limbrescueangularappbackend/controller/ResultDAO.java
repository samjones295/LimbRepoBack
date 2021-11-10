package com.limbrescue.limbrescueangularappbackend.controller;

import com.limbrescue.limbrescueangularappbackend.model.Result;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
@CrossOrigin(origins="http://localhost:8081")
@RestController
@RequestMapping("")
public class ResultDAO {
    //All attributes read from the properties file.
    private String table;
    private static final Properties p = new Properties();
    private FileReader reader;
    private DBConnection dbConnection;
    public ResultDAO()  {
        //Determine what file to read
        try {
            reader = new FileReader("src/main/resources/application.properties");
        } catch (FileNotFoundException e) {
            System.out.println("Cannot find the file");
        }
        try {
            p.load(reader);
        } catch (IOException e) {
            System.out.println("Cannot load file");
        }
        table = p.getProperty("spring.datasource.ResultTable");
        dbConnection = new DBConnection();
    }

    /**
     * Retrieves all the elements of the results table and stores it in an array list.
     *
     * @return
     *          An arraylist containing the results table.
     * @throws SQLException
     */
    @GetMapping("/allresults")
    @ResponseBody
    public List<Result> getAllResults() throws SQLException {
        Connection connection = dbConnection.getConnection();
        String sql = "SELECT * FROM " + table;
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet result = statement.executeQuery();
        List<Result> results = new ArrayList<>();
        while (result.next()) {
            Result res = new Result(result.getInt("id"), result.getInt("group_id"), result.getString("algorithm"),
                    result.getInt("ran_by"), result.getString("status"), result.getString("comments"));
            results.add(res);
        }
        connection.close();
        return results;
    }

    /**
     * Retrieves a single result based on the ID.
     *
     * @param id
     *          The ID to be retrieved
     * @return
     *          A pointer to a tuple in the results table.
     * @throws SQLException
     */
    @GetMapping("/singleresult/{id}")
    @ResponseBody
    public Result getResult(@PathVariable("id") int id) throws SQLException{
        Connection connection = dbConnection.getConnection();
        String sql = "SELECT * FROM " + table + " WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, id);
        ResultSet result = statement.executeQuery();
        Result res = null;
        if (result.next()) {
            res = new Result();
            res.setId(id);
            res.setGroup_id(result.getInt("group_id"));
            res.setAlgorithm(result.getString("algorithm"));
            res.setRan_by(result.getInt("ran_by"));
            res.setStatus(result.getString("status"));
            res.setComments(result.getString("comments"));
        }
        connection.close();
        return res;
    }

    /**
     * Inserts a result to the table.
     *
     * @param res
     *          The result to be inserted.
     * @throws SQLException
     */
    @PostMapping(path = "/result")
    @ResponseBody
    public void insertResult(@RequestBody Result res) throws SQLException{
        Connection connection = dbConnection.getConnection();
        if (getResult(res.getId()) != null) {
            updateResult(res, res.getId());
        }
        else {
            String sql = "INSERT INTO " + table + " VALUES(" + res.getId() + ", " + res.getGroup_id() + ", '" +
                    res.getAlgorithm() + "', " + res.getRan_by() + ", '" + res.getStatus() + "', '" + res.getComments() + "')";
            Statement statement = connection.prepareStatement(sql);
//            statement.setInt(1, res.getId());
//            statement.setInt(2, res.getGroup_id());
//            statement.setString(3, res.getAlgorithm());
//            statement.setInt(4, res.getRan_by());
//            statement.setString(5, res.getStatus());
//            statement.setString(6, res.getComments());
            statement.executeUpdate(sql);
        }
        connection.close();
    }

    /**
     * Updates a result based on the ID.
     *
     * @param res
     *          The variable values of the columns.
     * @param id
     *          The result ID to be updated.
     * @return
     *          The updated result.
     * @throws SQLException
     */
    @PutMapping(path="/result/{id}")
    @ResponseBody
    public Result updateResult(@RequestBody Result res, @PathVariable("id") int id) throws SQLException{
        Connection connection = dbConnection.getConnection();
        String sql = "UPDATE " + table + " SET group_id = ?, algorithm = ?, ran_by = ?, status = ?, comments = ?, WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, res.getGroup_id());
        statement.setString(2, res.getAlgorithm());
        statement.setInt(3, res.getRan_by());
        statement.setString(4, res.getStatus());
        statement.setString(5, res.getComments());
        statement.setInt(6, id);
        ResultSet result = statement.executeQuery();
        res.setGroup_id(result.getInt("group_id"));
        res.setAlgorithm(result.getString("algorithm"));
        res.setRan_by(result.getInt("ran_by"));
        res.setStatus(result.getString("status"));
        connection.close();
        return res;
    }

    /**
     * Updates the comments of a result.
     *
     * @param res
     *          The result to be updated
     * @param id
     *          The id to be updated.
     * @param comment
     *          The updated comment.
     * @return
     *          The updated result.
     * @throws SQLException
     */
    @PutMapping("/resultcomment/{id}/{comment}")
    @ResponseBody
    public Result updateComments(@RequestBody Result res, @PathVariable("id") int id, @PathVariable("comment") String comment) throws SQLException {
        Connection connection = dbConnection.getConnection();
        String sql = "UPDATE " + table + " SET comments = ? WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, comment);
        statement.setInt(2, id);
        ResultSet result = statement.executeQuery();
        res.setComments(result.getString("comments"));
        connection.close();
        return res;
    }

    /**
     * Deletes a result based on the ID.
     *
     * @param id
     *          The ID to be deleted.
     * @throws SQLException
     */
    @DeleteMapping("/result/{id}")
    @ResponseBody
    public void deleteResult(@PathVariable("id") int id) throws SQLException{
        Connection connection = dbConnection.getConnection();
        String sql = "DELETE FROM " + table + " WHERE id = " + id;
        Statement statement = connection.prepareStatement(sql);
        //statement.setInt(1, id);
        statement.executeUpdate(sql);
        connection.close();
    }

    /**
     * Exports the results to a .csv file.
     *
     * @throws SQLException
     */
    @GetMapping(path = "/viewreport")
    @ResponseBody
    public void exportResultsToCSV() throws SQLException{
        Connection connection = dbConnection.getConnection();
        String outputFile = p.getProperty("spring.datasource.OutputFile");
        String sql = "(SELECT 'ID', 'Group ID', 'Algorithm', 'Ran By', 'Status', 'Comments') UNION (SELECT * FROM " + table +
                " ) INTO OUTFILE '" + outputFile + "' FIELDS ENCLOSED BY '\"' TERMINATED BY ',' ESCAPED BY '\"' LINES TERMINATED BY '\n";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.executeQuery();
        connection.close();
    }
}
