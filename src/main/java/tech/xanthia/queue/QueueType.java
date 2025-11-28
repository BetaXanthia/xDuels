package tech.xanthia.queue;

/**
 * Queue types for matchmaking.
 * 
 * @author Xanthia
 * @version 1.0.0
 * @since 1.0.0
 */
public enum QueueType {
    
    /**
     * Unranked 1v1
     */
    UNRANKED_1V1("Unranked 1v1", false, 1),
    
    /**
     * Ranked 1v1
     */
    RANKED_1V1("Ranked 1v1", true, 1),
    
    /**
     * Unranked 2v2
     */
    UNRANKED_2V2("Unranked 2v2", false, 2),
    
    /**
     * Ranked 2v2
     */
    RANKED_2V2("Ranked 2v2", true, 2),
    
    /**
     * Unranked 3v3
     */
    UNRANKED_3V3("Unranked 3v3", false, 3),
    
    /**
     * Ranked 3v3
     */
    RANKED_3V3("Ranked 3v3", true, 3),
    
    /**
     * Unranked 4v4
     */
    UNRANKED_4V4("Unranked 4v4", false, 4),
    
    /**
     * Ranked 4v4
     */
    RANKED_4V4("Ranked 4v4", true, 4),
    
    /**
     * Unranked 5v5
     */
    UNRANKED_5V5("Unranked 5v5", false, 5),
    
    /**
     * Ranked 5v5
     */
    RANKED_5V5("Ranked 5v5", true, 5),
    
    /**
     * Party vs Party (size determined dynamically)
     */
    PARTY_VS_PARTY("Party vs Party", false, -1),
    
    /**
     * Party FFA (size determined dynamically)
     */
    PARTY_FFA("Party FFA", false, -1);
    
    private final String displayName;
    private final boolean ranked;
    private final int teamSize;
    
    QueueType(String displayName, boolean ranked, int teamSize) {
        this.displayName = displayName;
        this.ranked = ranked;
        this.teamSize = teamSize;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isRanked() {
        return ranked;
    }
    
    public int getTeamSize() {
        return teamSize;
    }
    
    public boolean isPartyQueue() {
        return this == PARTY_VS_PARTY || this == PARTY_FFA;
    }
}
