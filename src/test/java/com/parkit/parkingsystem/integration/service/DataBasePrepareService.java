package com.parkit.parkingsystem.integration.service;

import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;

import java.sql.Connection;

import static com.parkit.parkingsystem.integration.constants.DBConstantsIT.RESET_PARKING_IT;
import static com.parkit.parkingsystem.integration.constants.DBConstantsIT.RESET_TICKET_IT;

public class DataBasePrepareService {

    DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();

    public void clearDataBaseEntries(){
        Connection connection = null;
        try{
            connection = dataBaseTestConfig.getConnection();

            //set parking entries to available
            connection.prepareStatement(RESET_PARKING_IT).execute();

            //clear ticket entries;
            connection.prepareStatement(RESET_TICKET_IT).execute();

        }catch(Exception e){
            e.printStackTrace();
        }finally {
            dataBaseTestConfig.closeConnection(connection);
        }
    }

}
