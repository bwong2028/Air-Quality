package quality;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * This class represents the AirQuality system which populates a hashtable with states and counties 
 * and calculates various statistics related to air quality.
 * 
 * This class is a part of the AirQuality project.
 * 
 * @author Anna Lu
 * @author Srimathi Vadivel
 */

public class AirQuality 
{

    private State[] states; // hash table used to store states. This HT won't need rehashing.

    /**
     * **DO NOT MODIFY THIS METHOD**
     * Constructor creates a table of size 10.
     */

    public AirQuality () 
    {
        states  = new State[10];
    }     

    /**
     ** *DO NOT MODIFY THIS METHOD**
     * Returns the hash table.
     * @return the value held to represent the hash table
     */
    public State[] getHashTable() 
    {
        return states;
    }
    
    /**
     * 
     * DO NOT UPDATE THIS METHOD
     * 
     * This method populates the hashtable with the information from the inputFile parameter.
     * It is expected to insert a state and then the counties into each state.
     * 
     * Once a state is added, use the load factor to check if a resize of the hash table
     * needs to occur.
     * 
     * @param inputLine A line from the inputFile with the following format:
     * State Name,County Name,AQI,Latitude,Longitude,Pollutant Name,Color
     */

    public void buildTable ( String inputFile ) {
        
        StdIn.setFile(inputFile); // opens the inputFile to be read
        StdIn.readLine();         // skips header
        
        while ( !StdIn.isEmpty() ) 
        {

            String line = StdIn.readLine(); 
            State s = addState( line );
            addCountyAndPollutant(s, line );
        }
    }
    
    /**
     * Inserts a single State into the hash table states.
     * 
     * Note: No duplicate states allowed. If the state is already present, simply
     * return the State object. Otherwise, insert at the front of the list.
     * 
     * @param inputLine A line from the inputFile with the following format:
     * State Name,County Name,AQI,Latitude,Longitude,Pollutant Name,Color
     * 
     * USE: Math.abs("State Name".hashCode()) as the key into the states hash table.
     * USE: hash function as: hash(key) = key % array length
     * 
     * @return the State object if already present in the table or the newly created
     * State object inserted.
     */

    public State addState ( String inputLine ) 
    {
        String[] token = inputLine.split(","); 
        String stateName = token[0]; 
        int hashIndex =  Math.abs(stateName.hashCode()) % states.length; 

        State currentState = states[hashIndex];
        while (currentState != null) 
        {
            if (currentState.getName().equals(stateName))
                return currentState;

            currentState = currentState.getNext();
        }

        State nState = new State(stateName);

        nState.setNext(states[hashIndex]);
        states[hashIndex] = nState;

        return nState;
    }
    
    /**
     * Returns true if the counties hash table (within State) needs to be resized (re-hashed) 
     *
     * Resize the hash table when (number of counties)/(array size) >= loadFactor
     * 
     * @return true if resizing needs to happen, false otherwise
     */

     public boolean checkCountiesHTLoadFactor ( State state ) 
     {
        int CT = state.getNumberOfCounties();
        int countiesTableSize =  state.getCounties().length;
        int LF = state.getLoadFactor();

        return (CT >= (countiesTableSize * LF));
	 
    }

    /**
     * Resizes (rehashes) the State's counties hashtable by doubling its size.
     * 
     * USE: county.hashCode() as the key into the State's counties hash table.
     */
    public void rehash ( State state ) 
    {
        County[] old = state.getCounties();

        int newHashSize = (old.length) * 2;
        County[] newHashTable = new County[newHashSize];

        for (int i = 0; i < old.length; i++) 
        {
            County curr = old[i];
            while (curr != null) 
            {
                County county = curr.getNext();
                int x = Math.abs(curr.getName().hashCode());
                int index = x % newHashSize;

                curr.setNext(newHashTable[index]);
                newHashTable[index] = curr;
                curr = county;
            }
        }
        state.setCounties(newHashTable);
    }

    /**
     * This method:
     * 1) Inserts the county (from the input line) into State, if not already present.
     *    Check the State's counties hash table load factor after inserting. The hash table may need
     *    to be resized.
     * 
     * 2) Then inserts the pollutant (from the input line) into County (from the input line), if not already present.
     *    If pollutant is present, update AQI.
     * 
     * Note: no duplicate counties in the State.
     * Note: no duplicate pollutants in the County.
     * 
     * @param inputLine A line from the inputFile with the following format:
     * State Name,County Name,AQI,Latitude,Longitude,Pollutant Name,Color
     * 
     * USE: Math.abs("County Name".hashCode()) as the key into the State's counties hash table.
     * USE: the hash function as: hash(key) = key % array length
     */

    public void addCountyAndPollutant (State state, String inputLine ) 
    {
        // WRITE YOUR CODE HERE
        String[] tokens = inputLine.split(",");
        String countyName = tokens[1]; 
        int aqi = Integer.parseInt(tokens[2]); 
        double latitude = Double.parseDouble(tokens[3]);
        double longitude = Double.parseDouble(tokens[4]); 
        String pollutantName = tokens[5]; 
        String color = tokens[6];
        County existing = null;

        int i = Math.abs(countyName.hashCode()) % state.getCounties().length;
    
        County pointer = state.getCounties()[i];
        while (pointer != null) 
        {
            String abc = pointer.getName();
            if (abc.equals(countyName))
            {
                existing = pointer;
                break; 
            }
            pointer = pointer.getNext(); 
        }
    
        if (existing == null) 
        {
            existing = new County(countyName, latitude, longitude, null);
            state.addCounty(existing); 
    
            if (checkCountiesHTLoadFactor(state))
                rehash(state);
        }

        ArrayList<Pollutant> list = existing.getPollutants();

        Pollutant existing2 = null;
    
        for (Pollutant p : list) 
        {
            String pName = p.getName();
            if (pName.equals(pollutantName)) 
            {
                existing2 = p;

                break;
            }
        }
    
        if (existing2 != null)
        {
            existing2.setAQI(aqi);
            existing2.setColor(color);
        }
        else
        {
            Pollutant xyz = new Pollutant(pollutantName, aqi, color);
            list.add(xyz);
        }
    }

    /**
     * Sets states' simple stats AQI for each State in the hash table.
     */
    public void setStatesAQIStats() 
    {
	// WRITE YOUR CODE HERE
        for (State state : states) 
        { 
            while (state != null)
            {
                County lowestAQIc = null;
                County highestAQIc = null;
                int totalAQI = 0;
                int highestAQI = Integer.MIN_VALUE;
                int totalCount = 0;
                int lowestAQI = Integer.MAX_VALUE;
                
                County[] pointer1 = state.getCounties();
                
                for (County c : pointer1) 
                {
                    while (c != null) 
                    {
                        for (Pollutant p :  c.getPollutants()) 
                        {
                            int tempaqi = p.getAQI(); 
                            totalAQI = totalAQI + tempaqi;
                            totalCount = totalCount + 1;

                            if (tempaqi > highestAQI)
                            {
                                highestAQI = tempaqi;
                                highestAQIc = c;
                            }
                        
                            if (tempaqi < lowestAQI) 
                            {
                                lowestAQI = tempaqi;
                                lowestAQIc = c;
                            }
                        }
                        c = c.getNext();
                    }
                }
                if (totalCount >0)
                {
                    state.setAvgAQI((double) totalAQI / totalCount);
                }
                
                state.setHighestAQI(highestAQIc);
                state.setLowestAQI(lowestAQIc);

                state = state.getNext();
            }
        }
    }

    /**
     * In this method you will find all the counties within a state that have the same parameter name
     * and above the AQI threshold.
     * 
     * @param stateName The name of the state
     * @param pollutantName The parameter name to filter by
     * @param AQIThreshold The AQI threshold
     * @return ArrayList<County> List of counties that meet the criteria
     */

    public ArrayList<County> meetsThreshold(String stateName, String pollutantName, int AQIThreshold) 
    {
        // WRITE YOUR CODE HERE
        ArrayList<County> resultingList = new ArrayList<>(); 

        State target = null;

        for (State state : states) 
        {
            while (state != null)
            {
                String x = state.getName();
                if (x.equals(stateName) ) 
                {
                    target = state;
                    break;
                }
                state = state.getNext();
            }
            if (target != null)
                break;
        }

        if (target == null) 
            return resultingList; 
        
        County[] countiesList = target.getCounties();
        for (County c : countiesList) 
        {
            while (c != null) 
            {
                ArrayList<Pollutant> pollutantList = c.getPollutants();
                for (Pollutant p : pollutantList) 
                {
                    String tempName = p.getName();
                   
                    if (tempName.equals(pollutantName) && p.getAQI() >= AQIThreshold) 
                    {
                        resultingList.add(c);

                        break; 
                    }
                }
                c = c.getNext(); 
            }
        }
        return resultingList; 
    } 
}
