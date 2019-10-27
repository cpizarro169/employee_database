package sample;

import java.io.FileNotFoundException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/*
 * This class represents a JavaFX window with
 * table (TableView) to display EmployeeRecord
 * objects. Every record has all properties that
 * belong to any employee, so some properties are
 * null (empty cell). For example, SalariedEmployee
 * doesn't have grossSales property, so this column
 * is empty. 
 * To add new employee the user needs to click on
 * "Add" button and new modal dialog appears. This dialog
 * contains all required fields (including date picker for 
 * birthdate and combobox to select predefined employee type)
 * to add one of 4 possible employees. It doesn't need to 
 * fill out all fields, for example if the user needs to add
 * salaried employee, then tfGrossSales will be ignored.
 * There is also last column "Payroll", which is filled out
 * automatically by calling earnings() method from Employee
 * abstract class. 
 * When the user adds new employee, method setEmployees(..)
 * is invoked to update table. This method connects to the
 * SQLite database to retrieve all EmployeeRecord objects.
 * Generally, EmployeeRecord class encapsulates 1 single row
 * in a table, so it's more convenient than existing Employee
 * class, which even doen't contain departmentName and employeeType
 * properties.
 * Another way is to create 4 tables for every type of employee.
 */
public class mydatabase extends Application {

	// Main method to run the program.
	public static void main(String[] args) {
		launch(args); // Invokes start() method.
	}

	@Override
	public void start(Stage stage) throws Exception {

		// Create container (or parent) panel to add table and button 'Add'.
		BorderPane parent = new BorderPane();

		// This is dynamic arraylist to keep track of the table records. Once arraylist
		// updated - tableview also updated.
		ObservableList<sample.EmployeeRecord> employees = FXCollections.observableArrayList();

		// Create table view to display all employees.
		TableView<sample.EmployeeRecord> table = new TableView<>(employees);

		/*
		 * Here all 14 columns are created to bind them with properties of EmployeeRecord.
		 * For example, there is property 'firstName', so column header 'First Name' is bound
		 * with the 'firstName'.
		 */
		TableColumn<sample.EmployeeRecord, String> col1 = new TableColumn<>("SSN");
		col1.setMinWidth(80);
		col1.setCellValueFactory(new PropertyValueFactory<sample.EmployeeRecord, String>("socialSecurityNumber"));
		TableColumn<sample.EmployeeRecord, String> col2 = new TableColumn<>("First Name");
		col2.setCellValueFactory(new PropertyValueFactory<sample.EmployeeRecord, String>("firstName"));
		TableColumn<sample.EmployeeRecord, String> col3 = new TableColumn<>("Last Name");
		col3.setCellValueFactory(new PropertyValueFactory<sample.EmployeeRecord, String>("lastName"));
		TableColumn<sample.EmployeeRecord, String> col4 = new TableColumn<>("Birthday");
		col4.setMinWidth(60);
		col4.setCellValueFactory(new PropertyValueFactory<sample.EmployeeRecord, String>("birthday"));
		TableColumn<sample.EmployeeRecord, String> col5 = new TableColumn<>("Employee Type");
		col5.setMinWidth(170);
		col5.setCellValueFactory(new PropertyValueFactory<sample.EmployeeRecord, String>("employeeType"));
		TableColumn<sample.EmployeeRecord, String> col6 = new TableColumn<>("Dep-t");
		col6.setMaxWidth(65);
		col6.setCellValueFactory(new PropertyValueFactory<sample.EmployeeRecord, String>("departmentName"));
		TableColumn<sample.EmployeeRecord, String> col7 = new TableColumn<>("Weekly Salary");
		col7.setMinWidth(100);
		col7.setCellValueFactory(new PropertyValueFactory<sample.EmployeeRecord, String>("weeklySalary"));
		TableColumn<sample.EmployeeRecord, String> col8 = new TableColumn<>("Bonus");
		col8.setMaxWidth(50);
		col8.setCellValueFactory(new PropertyValueFactory<sample.EmployeeRecord, String>("bonus"));
		TableColumn<sample.EmployeeRecord, String> col9 = new TableColumn<>("Gross Sales");
		col9.setCellValueFactory(new PropertyValueFactory<sample.EmployeeRecord, String>("grossSales"));
		TableColumn<sample.EmployeeRecord, String> col10 = new TableColumn<>("Commission Rate");
		col10.setMinWidth(120);
		col10.setCellValueFactory(new PropertyValueFactory<sample.EmployeeRecord, String>("commissionRate"));
		TableColumn<sample.EmployeeRecord, String> col11 = new TableColumn<>("Base Salary");
		col11.setCellValueFactory(new PropertyValueFactory<sample.EmployeeRecord, String>("baseSalary"));
		TableColumn<sample.EmployeeRecord, String> col12 = new TableColumn<>("Hours");
		col12.setMaxWidth(40);
		col12.setCellValueFactory(new PropertyValueFactory<sample.EmployeeRecord, String>("hours"));
		TableColumn<sample.EmployeeRecord, String> col13 = new TableColumn<>("Wage");
		col13.setMaxWidth(40);
		col13.setCellValueFactory(new PropertyValueFactory<sample.EmployeeRecord, String>("wage"));
		TableColumn<sample.EmployeeRecord, String> col14 = new TableColumn<>("Payroll");
		col14.setCellValueFactory(new PropertyValueFactory<sample.EmployeeRecord, String>("payroll"));

		// Now all 14 columns should be added to the table.
		table.getColumns().add(col1);
		table.getColumns().add(col2);
		table.getColumns().add(col3);
		table.getColumns().add(col4);
		table.getColumns().add(col5);
		table.getColumns().add(col6);
		table.getColumns().add(col7);
		table.getColumns().add(col8);
		table.getColumns().add(col9);
		table.getColumns().add(col10);
		table.getColumns().add(col11);
		table.getColumns().add(col12);
		table.getColumns().add(col13);
		table.getColumns().add(col14);

		// Update records (all rows) in the table.
		setEmployees(employees, 0, null);

		// Button to add new employee.
		Button btnAddEmployee = new Button("Add Employee");

		// Add event handler to the button.
		// User clicks the button and modal
		// window is displayed to allow the 
		// user adding new employee.
		btnAddEmployee.setOnAction(e -> {
			Stage addEmpDialog = new Stage(); // New stage for modal window.

			VBox inputPanel = new VBox(5); // Parent panel for window with
			// spacing 5 between nodes.

			inputPanel.setPadding(new Insets(10)); // Add padding between
			// nodes and window border.

			// Add new scene for new stage with new dimension.
			Scene addEmpScene = new Scene(inputPanel, 300, 450);

			/*
			 * Add all textfields/combobox/datepicker.
			 */
			TextField tfSSN = new TextField();
			tfSSN.setTooltip(new Tooltip("Enter SSN"));
			tfSSN.setPromptText("Enter SSN");

			TextField tfFirstName = new TextField();
			tfFirstName.setTooltip(new Tooltip("Enter First Name"));
			tfFirstName.setPromptText("Enter First Name");

			TextField tfLastName = new TextField();
			tfLastName.setTooltip(new Tooltip("Enter Last Name"));
			tfLastName.setPromptText("Enter Last Name");

			DatePicker dpBirthday = new DatePicker(LocalDate.now());
			dpBirthday.setTooltip(new Tooltip("Select Birthday"));

			ComboBox<String> comboEmpType = new ComboBox<>();
			comboEmpType.setPromptText("Select Employee Type");
			comboEmpType.getItems().addAll("salariedEmployee", "commissionEmployee", 
					"basePlusCommissionEmployee", "hourlyEmployee");

			TextField tfDepartment = new TextField();
			tfDepartment.setTooltip(new Tooltip("Enter Department"));
			tfDepartment.setPromptText("Enter Department Name");

			TextField tfWeeklySalary = new TextField();
			tfWeeklySalary.setTooltip(new Tooltip("Enter Weekly Salary"));
			tfWeeklySalary.setPromptText("Enter Weekly Salary");

			TextField tfBonus = new TextField();
			tfBonus.setTooltip(new Tooltip("Enter Bonus"));
			tfBonus.setPromptText("Enter Bonus");

			TextField tfGrossSales = new TextField();
			tfGrossSales.setTooltip(new Tooltip("Enter Gross Sales"));
			tfGrossSales.setPromptText("Enter Gross Sales");

			TextField tfCommissionRate = new TextField();
			tfCommissionRate.setTooltip(new Tooltip("Enter Commission Rate"));
			tfCommissionRate.setPromptText("Enter Commission Rate");

			TextField tfBaseSalary = new TextField();
			tfBaseSalary.setTooltip(new Tooltip("Enter Base Salary"));
			tfBaseSalary.setPromptText("Enter Base Salary");

			TextField tfHours = new TextField();
			tfHours.setTooltip(new Tooltip("Enter Hours"));
			tfHours.setPromptText("Enter Hours");

			TextField tfWage = new TextField();
			tfWage.setTooltip(new Tooltip("Enter Wage"));
			tfWage.setPromptText("Enter Wage");

			// Add button to add new employee and close modal window.
			Button btnAdd = new Button("Add");

			// Add event handler to this button.
			btnAdd.setOnAction(ev -> {

				try {

					// Here we need to set all properties.
					sample.EmployeeRecord emp = new sample.EmployeeRecord();
					emp.setSocialSecurityNumber(tfSSN.getText().trim());
					emp.setFirstName(tfFirstName.getText().trim());
					emp.setLastName(tfLastName.getText().trim());
					emp.setBirthday(dpBirthday.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
					emp.setEmployeeType(comboEmpType.getSelectionModel().getSelectedItem());
					emp.setDepartmentName(tfDepartment.getText());
					emp.setWeeklySalary(Double.parseDouble(tfWeeklySalary.getText().trim()));
					if (emp.getWeeklySalary() < 0) { // Validation.
						throw new IllegalArgumentException("Weekly salary must be >= 0.0");
					}
					emp.setBonus(Double.parseDouble(tfBonus.getText().trim()));
					emp.setGrossSales(Integer.parseInt(tfGrossSales.getText().trim()));
					if (emp.getGrossSales() < 0) { // Validation.
						throw new IllegalArgumentException("Gross sales must be >= 0.0");
					}
					emp.setCommissionRate(Double.parseDouble(tfCommissionRate.getText().trim()));
					if (emp.getCommissionRate() <= 0.0 || emp.getCommissionRate() >= 1.0) { // Validation.
						throw new IllegalArgumentException("Commission rate must be > 0.0 and < 1.0");
					}
					emp.setBaseSalary(Double.parseDouble(tfBaseSalary.getText().trim()));
					if (emp.getBaseSalary() < 0) { // Validation.
						throw new IllegalArgumentException("Base salary must be >= 0.0");
					}
					emp.setHours(Integer.parseInt(tfHours.getText().trim()));
					if (emp.getHours() < 0 || emp.getHours() > 168) { // Validation.
						throw new IllegalArgumentException("Hours worked must be >= 0.0 and <= 168.0");
					}
					emp.setWage(Double.parseDouble(tfWage.getText().trim()));

					addEmployee(emp); // Call method to add employee and pass EmployeeRecord as argument.
					setEmployees(employees, 0, null); // Update table after new employee added.
					addEmpDialog.close(); // Close this dialog.

				} catch (Exception exc) { // This exception throws when user enters string instead of integer/double,
					// or validation failed, or user entered nothing, or some database exception.
					// In all cases above error dialog with explanation appears.

					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error Dialog");
					alert.setHeaderText("Something went wrong");
					alert.setContentText(exc.getMessage());
					alert.showAndWait();
				}
			});

			// Add all input components to the panel.
			inputPanel.getChildren().addAll(tfSSN, tfFirstName, tfLastName, dpBirthday, comboEmpType,
					tfDepartment, tfWeeklySalary, tfBonus, tfGrossSales, tfCommissionRate, tfBaseSalary,
					tfHours, tfWage, btnAdd);

			addEmpDialog.setScene(addEmpScene);
			addEmpDialog.setTitle("Add Employee");
			addEmpDialog.initOwner(stage);
			addEmpDialog.initModality(Modality.APPLICATION_MODAL); // Make this window modal (user cannot
			// use parent window until modal closes).

			addEmpDialog.showAndWait();
		});

		btnAddEmployee.setMinWidth(100);

		// Add combobox and textarea
		ComboBox<String> comboQueries = new ComboBox<>();
		comboQueries.getItems().addAll("Select all employees working in department SALES",
				"Select hourly employees working over 30 hours", "Select all commission "
						+ "employees in descending order of the commission rate", "All employees",
				"User defined query from text area");
		comboQueries.setPromptText("Select option:");
		TextArea ta = new TextArea();
		comboQueries.setOnAction(e -> {
			int sel = comboQueries.getSelectionModel().getSelectedIndex();
			if (sel == 0) {
				setEmployees(employees, 1, null);
			} else if (sel == 1) {
				setEmployees(employees, 2, null);
			} else if (sel == 2) {
				setEmployees(employees, 3, null);
			} else if (sel == 3) {// Need to display all employees.
				setEmployees(employees, 0, null);
			}
			else { // User defined query from text area.
				setEmployees(employees, 0, ta.getText().trim());
			}
		});		

		HBox hbox = new HBox(5);
		hbox.setPadding(new Insets(5));
		hbox.getChildren().addAll(btnAddEmployee, comboQueries, ta);

		parent.setCenter(table); // Place tableview to the center of panel.
		parent.setBottom(hbox); // Place button to the bottom of panel.

		Scene scene = new Scene(parent, 800, 600); // Main scene.

		stage.setScene(scene); // Set main scene to the stage.

		stage.show(); // Display stage.
	}

	// This method adds new employee to the database.
	public void addEmployee(sample.EmployeeRecord rec) throws FileNotFoundException, SQLException {

		// First, insert data into employees table.
		try (PreparedStatement stmt = sample.DBConnection.get().prepareStatement("insert into employees values (?,?,?,?,?,?)")) {
			stmt.setString(1, rec.getSocialSecurityNumber());
			stmt.setString(2, rec.getFirstName());
			stmt.setString(3, rec.getLastName());
			stmt.setString(4, rec.getBirthday());
			stmt.setString(5, rec.getEmployeeType());
			stmt.setString(6, rec.getDepartmentName());
			stmt.executeUpdate();
		}

		// Next, define type of employee and only after that 
		// build prepared statement. For example, SalariedEmployee
		// doesn't have CommissionRate, so stmt.setDouble(3, getBonus())
		// is used instead of stmt.setDouble(3, rec.getCommissionRate()),
		// it's important.
		if (rec.getEmployeeType().equals("salariedEmployee")) {
			try (PreparedStatement stmt = sample.DBConnection.get().prepareStatement
					("insert into salariedEmployees values (?,?,?)")) {
				stmt.setString(1, rec.getSocialSecurityNumber());
				stmt.setDouble(2, rec.getWeeklySalary());
				stmt.setDouble(3, rec.getBonus());
				stmt.executeUpdate();
			}
		} else if (rec.getEmployeeType().equals("commissionEmployee")) {
			try (PreparedStatement stmt = sample.DBConnection.get().prepareStatement
					("insert into commissionEmployees values (?,?,?,?)")) {
				stmt.setString(1, rec.getSocialSecurityNumber());
				stmt.setInt(2, rec.getGrossSales());
				stmt.setDouble(3, rec.getCommissionRate());
				stmt.setDouble(4, rec.getBonus());
				stmt.executeUpdate();
			}
		} else if (rec.getEmployeeType().equals("basePlusCommissionEmployee")) {
			try (PreparedStatement stmt = sample.DBConnection.get().prepareStatement
					("insert into basePlusCommissionEmployees values (?,?,?,?,?)")) {
				stmt.setString(1, rec.getSocialSecurityNumber());
				stmt.setInt(2, rec.getGrossSales());
				stmt.setDouble(3, rec.getCommissionRate());
				stmt.setDouble(4, rec.getBaseSalary());
				stmt.setDouble(5, rec.getBonus());
				stmt.executeUpdate();
			}
		} else if (rec.getEmployeeType().equals("hourlyEmployee")) {
			try (PreparedStatement stmt = sample.DBConnection.get().prepareStatement
					("insert into hourlyEmployees values (?,?,?,?)")) {
				stmt.setString(1, rec.getSocialSecurityNumber());
				stmt.setInt(2, rec.getHours());
				stmt.setDouble(3, rec.getWage());
				stmt.setDouble(4, rec.getBonus());
				stmt.executeUpdate();
			}
		}
	}

	// Method to update all records in the table.
	// First, clear records.
	public void setEmployees(ObservableList<sample.EmployeeRecord> employees, int query, String userDefQuery) {
		employees.clear(); // Clear current records to avoid duplicates.
		try (Statement stmt = sample.DBConnection.get().createStatement()) {

			// Retrieve all employees.
			// Here we use UNION ALL statement to get records from all 5 tables. Since
			// this statement requires to have the same number of selected columns, we need
			// firstly to define table with the most number of columns (this is basePlusCommissionEmployees, 
			// it has 5 columns), so from now no less than 5 columns should be selected. If current
			// table doesn't have enough number of columns, just use null. For example, table
			// 'salariedEmployees' has only 3 columns, so 2 null at the end (5 - 3 = 2).
			try (ResultSet rs = stmt.executeQuery(userDefQuery == null ? "select e.socialSecurityNumber, e.firstName, "
					+ "e.lastName, e.birthday, e.employeeType, e.departmentName, s.weeklySalary, "
					+ "s.bonus, null, null from employees e inner join salariedEmployees s on "
					+ "e.socialSecurityNumber=s.socialSecurityNumber UNION ALL "
					+ "select e.socialSecurityNumber, e.firstName, "
					+ "e.lastName, e.birthday, e.employeeType, e.departmentName, c.grossSales, "
					+ "c.commissionRate, c.bonus, null from employees e inner join commissionEmployees c on "
					+ "e.socialSecurityNumber=c.socialSecurityNumber UNION ALL "
					+ "select e.socialSecurityNumber, e.firstName, "
					+ "e.lastName, e.birthday, e.employeeType, e.departmentName, b.grossSales, "
					+ "b.commissionRate, b.baseSalary, b.bonus from employees e inner join "
					+ "basePlusCommissionEmployees b on "
					+ "e.socialSecurityNumber=b.socialSecurityNumber UNION ALL "
					+ "select e.socialSecurityNumber, e.firstName, "
					+ "e.lastName, e.birthday, e.employeeType, e.departmentName, h.hours, "
					+ "h.wage, h.bonus, null from employees e inner join "
					+ "hourlyEmployees h on "
					+ "e.socialSecurityNumber=h.socialSecurityNumber" : userDefQuery)) {

				while (rs.next()) { // Iterate over all retrieved records.

					sample.EmployeeRecord rec = new sample.EmployeeRecord();
					rec.setSocialSecurityNumber(rs.getString(1));
					rec.setFirstName(rs.getString(2));
					rec.setLastName(rs.getString(3));
					rec.setBirthday(rs.getString(4));
					rec.setEmployeeType(rs.getString(5));
					rec.setDepartmentName(rs.getString(6));

					// If query 1 from combobox is selected.
					if (query == 1 && !rec.getDepartmentName().equalsIgnoreCase("sales")) {
						continue;
					}

					// If query 2 from combobox is selected.
					if (query == 2 && (!rec.getEmployeeType().equals("hourlyEmployee") || rs.getInt(7) <= 30)) {
						continue;
					}

					// If query 2 from combobox is selected.
					if (query == 3 && !rec.getEmployeeType().equals("commissionEmployee")) {
						continue;
					}

					sample.Employee emp = null;

					try {

						switch (rec.getEmployeeType()) { // Define employee type to get
						// right data. For example, salariedEmployee has weeklySalary under
						// columnIndex 7, whilst commissionEmployee has grossSales under index 7.
						// This is a big difference.

						case "salariedEmployee":
							rec.setWeeklySalary(rs.getDouble(7));
							rec.setBonus(rs.getDouble(8));

							// Here our employee is SalariedEmployee, so only weekly salary is important
							// to calculate earnings (we don't need SSN, first name and last name).
							emp = new sample.SalariedEmployee(null, null, null, rec.getWeeklySalary());
							break;
						case "commissionEmployee":
							rec.setGrossSales(rs.getInt(7));
							rec.setCommissionRate(rs.getDouble(8));
							rec.setBonus(rs.getDouble(9));

							// Tack C: over $10 000 add $100 bonus.
							if (rec.getGrossSales() > 10000) {
								rec.setBonus(rec.getBonus() + 100);
							}
							emp = new sample.CommissionEmployee(null, null, null, rec.getGrossSales(), rec.getCommissionRate());
							break;
						case "basePlusCommissionEmployee":
							rec.setGrossSales(rs.getInt(7));
							rec.setCommissionRate(rs.getDouble(8));
							rec.setBaseSalary(rs.getDouble(9));

							//  increase by 10%.
							rec.setBaseSalary(rec.getBaseSalary() + rec.getBaseSalary() * 0.1);
							rec.setBonus(rs.getDouble(10));
							emp = new sample.BasePlusCommissionEmployee(null, null, null, rec.getGrossSales(), rec.getCommissionRate(),
									rec.getBaseSalary());
							break;
						case "hourlyEmployee":
							rec.setHours(rs.getInt(7));
							rec.setWage(rs.getDouble(8));
							rec.setBonus(rs.getDouble(9));
							emp = new sample.HourlyEmployee(null, null, null, rec.getWage(), rec.getHours());
						}
					} catch (Exception exc2) {} // To allow filling out not all columns in table
					// (important for user defined queries, for example select * from employees).
					// Exception throws when rs.getInt(7), if query expects only 6 columns.

					if (emp != null) { // Check for null if there is a mistake in employee type.
						rec.setPayroll(emp.earnings()); // Set payroll by calculating earnings.
					}

					//  bonus for current month.
					LocalDate birhtday = LocalDate.parse(rec.getBirthday(), DateTimeFormatter.ofPattern("yyyy-M-d"));
					if (birhtday.getMonth().equals(LocalDate.now().getMonth())) {
						try {
							rec.setBonus(rec.getBonus() + 100.0); // Add $100 bonus.
						} catch (Exception exc2) {} // To allow filling out not all columns in table
						// (important for user defined queries, for example select * from employees).
					}

					employees.add(rec); // Add new record to the list.
				}
			}

			if (query == 3) { // Sort in descending order of commission rate.
				Collections.sort(employees, (e1, e2) -> 
				Double.compare(e2.getCommissionRate(), e1.getCommissionRate()));
			}

		} catch (FileNotFoundException | SQLException e) { // If database not found or smth
			// other problem with connection.

			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error Dialog");
			alert.setHeaderText("Something went wrong");
			alert.setContentText(e.getMessage());
			alert.showAndWait();
		} 
	}

} // end of class
