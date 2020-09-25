class DGRemote {
    companion object {
        public fun identifyProtocolVersion(data: String) : String {
            return if (data.contains("aStrengthRangeMax")) {
                "V2"
            } else {
                "V1"
            }
        }
    }
}