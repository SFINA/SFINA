/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package power.input;

/**
 *
 * @author Ben
 */
public enum PowerLinkState {
    ID,
    CURRENT,
    POWER_FLOW_FROM_REAL,
    POWER_FLOW_FROM_REACTIVE,
    POWER_FLOW_TO_REAL,
    POWER_FLOW_TO_REACTIVE,
    LOSS_REAL,
    LOSS_REACTIVE,
    RESISTANCE,
    REACTANCE,
    SUSCEPTANCE,
    RATE_A,         // MVA rating A (long term rating)
    RATE_B,         // MVA rating B (short term rating)
    RATE_C,         // MVA rating C (emergency rating)
    TAP_RATIO,      // transformer off nominal turns ratio
    ANGLE_SHIFT,
    ANGLE_DIFFERENCE_MIN,
    ANGLE_DIFFERENCE_MAX,
}