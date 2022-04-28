package Manager.Requests;

public class AnalysisType {
    public enum AnalysisTypeEnum {
        POS,
        CONSTITUENCY,
        DEPENDENCY
    }

    public static AnalysisTypeEnum getAnalysisType(String type) {
        if (type.equals(AnalysisTypeEnum.POS.toString())){
            return AnalysisTypeEnum.POS;
        }
        if (type.equals(AnalysisTypeEnum.CONSTITUENCY.toString())){
            return AnalysisTypeEnum.CONSTITUENCY;
        }
        if (type.equals(AnalysisTypeEnum.DEPENDENCY.toString())){
            return AnalysisTypeEnum.DEPENDENCY;
        }
        return null;
    }
}
