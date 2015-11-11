package com.semdog.ultranaut.mathematics;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.semdog.ultranaut.universe.Universe;

/**
 * This is a class full of orbital mathematics (it's all
 * packaged into one method, however.)
 * 
 * @author Sam
 */

public class OrbitalHelper {

	/**
	 * This method is the reason the OrbitalHelper class exists - to help
	 * determine the various quantities involved in orbits without each class
	 * needing the code.
	 * 
	 * It takes in some orbital states and returns a whole bunch of information
	 * packaged into a float array
	 */
	public static float[] computeOrbit(Vector2 orbiteePosition, Vector2 orbiterPosition, Vector2 orbiterVelocity, float orbiteeMass) {
		Vector2 offset = orbiterPosition.sub(orbiteePosition);

		float mu = Universe.GRAVCONSTANT * orbiteeMass;

		float angularMomentum = offset.crs(orbiterVelocity);
		float eccX = (orbiterVelocity.y * angularMomentum) / mu - (offset.x / offset.len());
		float eccY = -(orbiterVelocity.x * angularMomentum) / mu - (offset.y / offset.len());
		Vector2 eccentricityVector = new Vector2(eccX, eccY);
		float eccentricity = eccentricityVector.len();

		float energy = orbiterVelocity.len() * orbiterVelocity.len() / 2 - mu / offset.len();

		float semiMajorAxis = -mu / (2 * energy);
		float semiMinorAxis = (float) (semiMajorAxis * Math.sqrt(1 - (eccentricity * eccentricity)));
		float apoapsis = semiMajorAxis * (1 + eccentricity);
		float periapsis = semiMajorAxis * (1 - eccentricity);

		float trueAnomaly = 0;

		if (angularMomentum > 0) {
			if (offset.dot(orbiterVelocity) < 0) {
				trueAnomaly = (float) Math.acos(eccentricityVector.dot(offset) / (eccentricity * offset.len()));
			} else {
				trueAnomaly = MathUtils.PI2 - (float) Math.acos(eccentricityVector.dot(offset) / (eccentricity * offset.len()));
			}
		} else {
			if (offset.dot(orbiterVelocity) < 0) {
				trueAnomaly = MathUtils.PI2 - (float) Math.acos(eccentricityVector.dot(offset) / (eccentricity * offset.len()));
			} else {
				trueAnomaly = (float) Math.acos(eccentricityVector.dot(offset) / (eccentricity * offset.len()));
			}
		}

		float sinE = (float) (MathUtils.sin(trueAnomaly) * Math.sqrt(1 - (eccentricity * eccentricity)) / (1 + eccentricity * MathUtils.cos(trueAnomaly)));
		float cosE = (float) ((eccentricity + MathUtils.cos(trueAnomaly)) / (1 + eccentricity * MathUtils.cos(trueAnomaly)));
		float eccentricAnomaly = MathUtils.atan2(sinE, cosE);
		
		float orbitalPeriod = MathUtils.PI2 * (float)Math.sqrt(Math.pow(semiMajorAxis, 3) / mu);

		float[] results = new float[10];
		results[0] = angularMomentum;
		results[1] = eccentricity;
		results[2] = energy;
		results[3] = semiMajorAxis;
		results[4] = semiMinorAxis;
		results[5] = apoapsis;
		results[6] = periapsis;
		results[7] = trueAnomaly;
		results[8] = eccentricAnomaly;
		results[9] = orbitalPeriod;
		return results;
	}
}
