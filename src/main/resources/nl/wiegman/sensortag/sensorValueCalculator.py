#import sys
#import time

def floatfromhex(h):
    t = float.fromhex(h)
    if t > float.fromhex('7FFF'):
        t = -(float.fromhex('FFFF') - t)
        pass
    return t

def calculateTargetTemperature(objT, ambT):
    m_tmpAmb = ambT/128.0
    Vobj2 = objT * 0.00000015625
    Tdie2 = m_tmpAmb + 273.15
    S0 = 6.4E-14            # Calibration factor
    a1 = 1.75E-3
    a2 = -1.678E-5
    b0 = -2.94E-5
    b1 = -5.7E-7
    b2 = 4.63E-9
    c2 = 13.4
    Tref = 298.15
    S = S0*(1+a1*(Tdie2 - Tref)+a2*pow((Tdie2 - Tref), 2))
    Vos = b0 + b1*(Tdie2 - Tref) + b2*pow((Tdie2 - Tref), 2)
    fObj = (Vobj2 - Vos) + c2*pow((Vobj2 - Vos), 2)
    tObj = pow(pow(Tdie2,4) + (fObj/S), .25)
    tObj = (tObj - 273.15)
    return tObj

def calculateAmbientTemperature(ambT):
    return ambT/128.0

def calculateHumidity(rawT, rawH):
    # calculate temperature [deg C]
    t = -46.85 + 175.72/65536.0 * rawT

    rawH = float(int(rawH) & ~0x0003); # clear bits [1..0] (status bits)
    # calculate relative humidity [%RH]
    rh = -6.0 + 125.0/65536.0 * rawH # RH= -6 + 125 * SRH/2^16
    return rh

class SensorValueCalculator:

    def ambientTemperature(self, hex):
        rval = hex.split()
        ambT = floatfromhex(rval[3] + rval[2])
        return calculateAmbientTemperature(ambT)

    def targetTemperature(self, hex):
        rval = hex.split()
        objT = floatfromhex(rval[1] + rval[0])
        ambT = floatfromhex(rval[3] + rval[2])
        return calculateTargetTemperature(objT, ambT)

    def humidity(self, hex):
        rval = hex.split()
        pr = rval[1] + rval[0]
        rawT = floatfromhex(rval[1] + rval[0])
        rawH = floatfromhex(rval[3] + rval[2])
        return calculateHumidity(rawT, rawH)