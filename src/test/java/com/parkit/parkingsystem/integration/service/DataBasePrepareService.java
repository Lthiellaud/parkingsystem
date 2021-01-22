package com.parkit.parkingsystem.integration.service;

import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;

import static com.parkit.parkingsystem.integration.constants.DBConstantsIT.EMPTY_PARKING_IT;
import static com.parkit.parkingsystem.integration.constants.DBConstantsIT.RESET_TICKET_IT;

public class DataBasePrepareService {

    DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();

    public void clearDataBaseEntries(){

        try (Connection connection = dataBaseTestConfig.getConnection();
             //set parking entries to available
             PreparedStatement ps1 = connection.prepareStatement(EMPTY_PARKING_IT);
             //clear ticket entries;
             PreparedStatement ps2 = connection.prepareStatement(RESET_TICKET_IT)) {

            ps1.execute();
            ps2.execute();

        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
