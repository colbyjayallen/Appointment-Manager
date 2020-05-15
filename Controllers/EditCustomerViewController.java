package Controllers;

import DAO.CountryCityDao;
import DAO.CustomerDao;
import Models.Address;
import Models.City;
import Models.Customer;
import Utils.SessionManager;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewFocusModel;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author colby
 */
public class EditCustomerViewController extends BaseController implements Initializable {

    @FXML private TextField customerNameTextField = new TextField();
    @FXML private TextField addressTextField = new TextField();
    @FXML private TextField address2TextField = new TextField();
    @FXML private TextField postalCodeTextField = new TextField();
    @FXML private TextField phoneTextField = new TextField();
    @FXML private Label ErrorLabel = new Label();
    @FXML private Button exitButton = new Button();
    
    @FXML private Alert confirmationAlert = new Alert(Alert.AlertType.INFORMATION);
    
    @FXML private TableView<City> cityTable;
    @FXML private TableColumn<City, Integer> cityIdColumn;
    @FXML private TableColumn<City, String> cityNameColumn;
    @FXML private TableViewFocusModel<TableView<City>> focusedCity;
    private ObservableList<City> cities;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cities = CountryCityDao.getAllCities();
        
        cityIdColumn.setCellValueFactory(new PropertyValueFactory<>("cityId"));
        cityNameColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        cityTable.setItems(cities);
        
        confirmationAlert.setTitle("Successful Addition");
        confirmationAlert.setHeaderText("Successful!");
    }
    
    public void initData(Customer customer) {
        Address tempAddress = CustomerDao.getAddressById(customer.getAddressId());
        
        this.customerNameTextField.setText(customer.getCustomerName());
        this.addressTextField.setText(tempAddress.getAddress1());
        this.address2TextField.setText(tempAddress.getAddress2());
        this.postalCodeTextField.setText(tempAddress.getPostalCode());
        this.phoneTextField.setText(tempAddress.getPostalCode());
        
        int cityId = getCityIndexFromId(tempAddress.getCityId());
        if(cityId != -1) {
            this.cityTable.getSelectionModel().select(cityId);
        }
    }
    
    @FXML
    private void returnToCustomerList(ActionEvent ae) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/Views/AllCustomerView.fxml"));
        Parent addProductParent = loader.load();
        Scene addProductScene = new Scene(addProductParent);
        Stage window = (Stage) ((Node)ae.getSource()).getScene().getWindow();
        window.setScene(addProductScene);
        window.show();
    }
    
    @FXML
    private void modifyCustomer() {
        String result = validateCustomer();
        if(!result.equals("Ok")) {
            ErrorLabel.setText(result);
        }
        else {
            exitButton.fire();
        }
    }
    
    private String validateCustomer() {
        if(this.customerNameTextField.getText().isEmpty()) {
            return errorGeneratorMissing("Customer Name");
        }
        if(this.addressTextField.getText().isEmpty()) {
            return errorGeneratorMissing("Address");
        }
        if(this.address2TextField.getText().isEmpty()) {
            return errorGeneratorMissing("Address 2");
        }
        if(this.postalCodeTextField.getText().isEmpty()) {
            return errorGeneratorMissing("Postal Code");
        }
        if(this.phoneTextField.getText().isEmpty()) {
            return errorGeneratorMissing("Phone");
        }
        if(this.cityTable.getSelectionModel().isEmpty()) {
            return errorGeneratorMissing("City");
        }
        
        //validate all address portions are ok
        //validate all customer fields are ok
        if(this.customerNameTextField.getText().length() > 45) {
            return errorGeneratorExceedsLength("Customer Name", 45);
        }
        if(this.addressTextField.getText().length() > 50) {
            return errorGeneratorExceedsLength("Address", 50);
        }
        if(this.address2TextField.getText().length() > 50) {
            return errorGeneratorExceedsLength("Address 2", 50);
        }
        if(this.postalCodeTextField.getText().length() > 10) {
            return errorGeneratorExceedsLength("Postal Code", 10);
        }
        if(this.phoneTextField.getText().length() > 20) {
            return errorGeneratorExceedsLength("Phone", 20);
        }
        //check for duplicate address
        Address tempAdd = new Address(-1, addressTextField.getText(), address2TextField.getText(),this.cityTable.getSelectionModel().getSelectedItem().getCityId(), postalCodeTextField.getText(), phoneTextField.getText());
        if(!CustomerDao.verifyAddressExists(tempAdd)) {
            CustomerDao.createAddress(tempAdd, SessionManager.getSessionUser());
        }
        int tempAddressId = CustomerDao.getAddressId(tempAdd);
        if(tempAddressId == -1) {
            return "Error: Cannot find address.";
        }
        
        //add customer to db
        Customer tempCustomer = new Customer(-1, this.customerNameTextField.getText(), tempAddressId);
        if(CustomerDao.verifyCustomerExists(tempCustomer)) {
            return "Error: Specified Customer Already Exists in Database";
        }
        
        if(!CustomerDao.updateCustomerById(tempCustomer, SessionManager.getSessionUser())) {
            return "Error: Could not update customer to the Database";
        }
        
        //display a success alert
        confirmationAlert.setContentText("Customer: "+ tempCustomer.getCustomerName() +" successfully modified.");
        confirmationAlert.showAndWait();
        
        return "Ok";
    }
    
    private String errorGeneratorMissing(String field) {
        return "Error: '"+ field +"' is a required field";
    }
    
    private String errorGeneratorExceedsLength(String field, int maxLength) {
        return "Error: '"+ field +"' is too long. Max Length: "+ maxLength;
    }
    
    private int getCityIndexFromId(int cityId) {
        for(int i = 0; i < this.cities.size(); i++) {
            if(cities.get(i).getCityId() == cityId) {
                return i;
            }
        }
        return -1;
    }
    
}
