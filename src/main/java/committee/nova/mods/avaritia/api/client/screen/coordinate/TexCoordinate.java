package committee.nova.mods.avaritia.api.client.screen.coordinate;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 背景图片坐标配置, 坐标宽高度单位都为像素
 * from <a href="https://github.com/TinyTsuki/SakuraSignIn_MC">...</a>
 */
@Getter
@Setter
@Accessors(chain = true)
@SuppressWarnings("ConstantConditions")
public class TexCoordinate implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 特殊主题
     */
    private boolean special;
    /**
     * 纹理图片总宽度
     */
    private int totalWidth = 500;
    /**
     * 纹理图片总高度
     */
    private int totalHeight = 1000;
    /**
     * 周起始日
     * 7: 周日
     * 1: 周一
     * 2: 周二
     * 3: 周三
     * 4: 周四
     * 5: 周五
     * 6: 周六
     */
    private int weekStart = 7;

    /**
     * 文本颜色: 默认
     */
    private int textColorDefault = 0xFFDBDBDB;
    /**
     * 文本颜色: 本月日期
     */
    private int textColorCurrent = 0xFFFFFFFF;
    /**
     * 文本颜色: 可补签
     */
    private int textColorCanRepair = 0xFFFFFFBB;
    /**
     * 文本颜色: 今天
     */
    private int textColorToday = 0xFF474747;
    /**
     * 文本颜色: 日期
     */
    private int textColorDate = 0xFF000000;

    /**
     * 年份区域坐标
     */
    @NonNull
    private Coordinate yearCoordinate = new Coordinate().setX(128).setY(60).setWidth(92).setHeight(16);

    /**
     * 月份区域坐标
     */
    @NonNull
    private Coordinate monthCoordinate = new Coordinate().setX(320).setY(60).setWidth(62).setHeight(16);

    /**
     * 日期区域坐标
     */
    @NonNull
    private Coordinate cellCoordinate = new Coordinate().setX(73).setY(134).setWidth(36).setHeight(36);
    /**
     * 日期单元格左右边距
     */
    private int cellHMargin = 18;
    /**
     * 日期单元格上下边距
     */
    private int cellVMargin = 26;
    /**
     * 日期偏移
     */
    private int dateOffset = (int) this.cellCoordinate.getHeight();

    /**
     * 左箭头区域坐标
     */
    @NonNull
    private Coordinate leftArrowCoordinate = new Coordinate().setX(320).setY(60).setWidth(62).setHeight(16);

    /**
     * 右箭头区域坐标
     */
    @NonNull
    private Coordinate rightArrowCoordinate = new Coordinate().setX(320).setY(60).setWidth(62).setHeight(16);

    /**
     * 上箭头区域坐标
     */
    @NonNull
    private Coordinate upArrowCoordinate = new Coordinate().setX(128).setY(60).setWidth(92).setHeight(16);

    /**
     * 下箭头区域坐标
     */
    @NonNull
    private Coordinate downArrowCoordinate = new Coordinate().setX(128).setY(60).setWidth(92).setHeight(16);

    /**
     * 主题区域坐标
     */
    @NonNull
    private Coordinate themeCoordinate = new Coordinate().setX(300).setY(552).setWidth(40).setHeight(48);
    /**
     * 主题按钮左右边距
     */
    private int themeHMargin = 0;

    /**
     * 背景纹理坐标
     */
    @NonNull
    private Coordinate bgUV = new Coordinate().setU0(0).setV0(0).setUWidth(500).setVHeight(600);

    /**
     * 奖励弹出层纹理坐标
     */
    @NonNull
    private Coordinate tooltipUV = new Coordinate().setU0(0).setV0(784).setUWidth(300).setVHeight(96);
    /**
     * 弹出层单元格左右边距
     */
    private int tooltipCellHMargin = 5;
    /**
     * 弹出层单元格坐标
     */
    @NonNull
    private Coordinate tooltipCellCoordinate = new Coordinate().setX(15).setY(10).setWidth(42).setHeight(42);
    /**
     * 弹出层日期坐标
     */
    @NonNull
    private Coordinate tooltipDateCoordinate = new Coordinate().setX(110).setY(64).setWidth(50).setHeight(19);
    /**
     * 弹出层滚动条坐标
     */
    @NonNull
    private Coordinate tooltipScrollCoordinate = new Coordinate().setX(10).setY(93).setWidth(280).setHeight(1);
    /**
     * 箭头纹理坐标
     */
    @NonNull
    private Coordinate arrowUV = new Coordinate().setU0(0).setV0(600).setUWidth(40).setVHeight(40);
    /**
     * 箭头焦点纹理坐标
     */
    @NonNull
    private Coordinate arrowHoverUV = new Coordinate().setU0(0).setV0(600).setUWidth(40).setVHeight(40);
    /**
     * 箭头按下纹理坐标
     */
    @NonNull
    private Coordinate arrowTapUV = new Coordinate().setU0(40).setV0(600).setUWidth(40).setVHeight(40);

    /**
     * 未签到纹理坐标
     */
    @NonNull
    private Coordinate notSignedInUV = new Coordinate().setU0(80).setV0(600).setUWidth(40).setVHeight(40);
    /**
     * 已签到纹理坐标
     */
    @NonNull
    private Coordinate signedInUV = new Coordinate().setU0(120).setV0(600).setUWidth(40).setVHeight(40);
    /**
     * 已领奖纹理坐标
     */
    @NonNull
    private Coordinate rewardedUV = new Coordinate().setU0(160).setV0(600).setUWidth(40).setVHeight(40);

    /**
     * BUFF纹理坐标
     */
    @NonNull
    private Coordinate buffUV = new Coordinate().setU0(200).setV0(600).setUWidth(40).setVHeight(40);
    /**
     * 经验纹理坐标
     */
    @NonNull
    private Coordinate pointUV = new Coordinate().setU0(240).setV0(600).setUWidth(40).setVHeight(40);
    /**
     * 等级纹理坐标
     */
    @NonNull
    private Coordinate levelUV = new Coordinate().setU0(280).setV0(600).setUWidth(40).setVHeight(40);
    /**
     * 签到卡纹理坐标
     */
    @NonNull
    private Coordinate cardUV = new Coordinate().setU0(320).setV0(600).setUWidth(40).setVHeight(40);
    /**
     * 消息纹理坐标
     */
    @NonNull
    private Coordinate messageUV = new Coordinate().setU0(360).setV0(600).setUWidth(40).setVHeight(40);

    /**
     * 主题纹理坐标
     */
    @NonNull
    private Coordinate themeUV = new Coordinate().setU0(0).setV0(640).setUWidth(40).setVHeight(48);
    /**
     * 主题焦点纹理坐标
     */
    @NonNull
    private Coordinate themeHoverUV = new Coordinate().setU0(0).setV0(688).setUWidth(40).setVHeight(48);
    /**
     * 主题按下纹理坐标
     */
    @NonNull
    private Coordinate themeTapUV = new Coordinate().setU0(0).setV0(736).setUWidth(40).setVHeight(48);

    /**
     * 奖励配置页面背景纹理坐标
     */
    @NonNull
    private Coordinate optionBgUV = new Coordinate().setU0(0).setV0(880).setUWidth(120).setVHeight(120);

    /**
     * 帮助按钮纹理坐标
     */
    @NonNull
    private Coordinate helpUV = new Coordinate().setU0(120).setV0(880).setUWidth(40).setVHeight(40);

    /**
     * 下载按钮纹理坐标
     */
    @NonNull
    private Coordinate downloadUV = new Coordinate().setU0(160).setV0(880).setUWidth(40).setVHeight(40);

    /**
     * 上传按钮纹理坐标
     */
    @NonNull
    private Coordinate uploadUV = new Coordinate().setU0(200).setV0(880).setUWidth(40).setVHeight(40);

    /**
     * 文件夹按钮纹理坐标
     */
    @NonNull
    private Coordinate folderUV = new Coordinate().setU0(240).setV0(880).setUWidth(40).setVHeight(40);

    /**
     * 排序按钮纹理坐标
     */
    @NonNull
    private Coordinate sortUV = new Coordinate().setU0(280).setV0(880).setUWidth(40).setVHeight(40);

    /**
     * 签到按钮纹理坐标
     */
    @NonNull
    private Coordinate signInBtnUV = new Coordinate().setU0(120).setV0(920).setUWidth(60).setVHeight(60);

    /**
     * 奖励配置按钮纹理坐标
     */
    @NonNull
    private Coordinate rewardOptionBtnUV = new Coordinate().setU0(180).setV0(920).setUWidth(60).setVHeight(60);

    // region Always NON-NULL Getter

    public @NonNull Coordinate getYearCoordinate() {
        return yearCoordinate = yearCoordinate == null ? new Coordinate() : yearCoordinate;
    }

    public @NonNull Coordinate getMonthCoordinate() {
        return monthCoordinate = monthCoordinate == null ? new Coordinate() : monthCoordinate;
    }

    public @NonNull Coordinate getCellCoordinate() {
        return cellCoordinate = cellCoordinate == null ? new Coordinate() : cellCoordinate;
    }

    public @NonNull Coordinate getLeftArrowCoordinate() {
        return leftArrowCoordinate = leftArrowCoordinate == null ? new Coordinate() : leftArrowCoordinate;
    }

    public @NonNull Coordinate getRightArrowCoordinate() {
        return rightArrowCoordinate = rightArrowCoordinate == null ? new Coordinate() : rightArrowCoordinate;
    }

    public @NonNull Coordinate getUpArrowCoordinate() {
        return upArrowCoordinate = upArrowCoordinate == null ? new Coordinate() : upArrowCoordinate;
    }

    public @NonNull Coordinate getDownArrowCoordinate() {
        return downArrowCoordinate = downArrowCoordinate == null ? new Coordinate() : downArrowCoordinate;
    }

    public @NonNull Coordinate getThemeCoordinate() {
        return themeCoordinate = themeCoordinate == null ? new Coordinate() : themeCoordinate;
    }

    public @NonNull Coordinate getBgUV() {
        return bgUV = bgUV == null ? new Coordinate() : bgUV;
    }

    public @NonNull Coordinate getTooltipUV() {
        return tooltipUV = tooltipUV == null ? new Coordinate() : tooltipUV;
    }

    public @NonNull Coordinate getTooltipCellCoordinate() {
        return tooltipCellCoordinate = tooltipCellCoordinate == null ? new Coordinate() : tooltipCellCoordinate;
    }

    public @NonNull Coordinate getTooltipDateCoordinate() {
        return tooltipDateCoordinate = tooltipDateCoordinate == null ? new Coordinate() : tooltipDateCoordinate;
    }

    public @NonNull Coordinate getTooltipScrollCoordinate() {
        return tooltipScrollCoordinate = tooltipScrollCoordinate == null ? new Coordinate() : tooltipScrollCoordinate;
    }

    public @NonNull Coordinate getArrowUV() {
        return arrowUV = arrowUV == null ? new Coordinate() : arrowUV;
    }

    public @NonNull Coordinate getArrowHoverUV() {
        return arrowHoverUV = arrowHoverUV == null ? new Coordinate() : arrowHoverUV;
    }

    public @NonNull Coordinate getArrowTapUV() {
        return arrowTapUV = arrowTapUV == null ? new Coordinate() : arrowTapUV;
    }

    public @NonNull Coordinate getNotSignedInUV() {
        return notSignedInUV = notSignedInUV == null ? new Coordinate() : notSignedInUV;
    }

    public @NonNull Coordinate getSignedInUV() {
        return signedInUV = signedInUV == null ? new Coordinate() : signedInUV;
    }

    public @NonNull Coordinate getRewardedUV() {
        return rewardedUV = rewardedUV == null ? new Coordinate() : rewardedUV;
    }

    public @NonNull Coordinate getBuffUV() {
        return buffUV = buffUV == null ? new Coordinate() : buffUV;
    }

    public @NonNull Coordinate getPointUV() {
        return pointUV = pointUV == null ? new Coordinate() : pointUV;
    }

    public @NonNull Coordinate getLevelUV() {
        return levelUV = levelUV == null ? new Coordinate() : levelUV;
    }

    public @NonNull Coordinate getCardUV() {
        return cardUV = cardUV == null ? new Coordinate() : cardUV;
    }

    public @NonNull Coordinate getMessageUV() {
        return messageUV = messageUV == null ? new Coordinate() : messageUV;
    }

    public @NonNull Coordinate getThemeUV() {
        return themeUV = themeUV == null ? new Coordinate() : themeUV;
    }

    public @NonNull Coordinate getThemeHoverUV() {
        return themeHoverUV = themeHoverUV == null ? new Coordinate() : themeHoverUV;
    }

    public @NonNull Coordinate getThemeTapUV() {
        return themeTapUV = themeTapUV == null ? new Coordinate() : themeTapUV;
    }

    public @NonNull Coordinate getOptionBgUV() {
        return optionBgUV = optionBgUV == null ? new Coordinate() : optionBgUV;
    }

    public @NonNull Coordinate getHelpUV() {
        return helpUV = helpUV == null ? new Coordinate() : helpUV;
    }

    public @NonNull Coordinate getDownloadUV() {
        return downloadUV = downloadUV == null ? new Coordinate() : downloadUV;
    }

    public @NonNull Coordinate getUploadUV() {
        return uploadUV = uploadUV == null ? new Coordinate() : uploadUV;
    }

    public @NonNull Coordinate getFolderUV() {
        return folderUV = folderUV == null ? new Coordinate() : folderUV;
    }

    public @NonNull Coordinate getSortUV() {
        return sortUV = sortUV == null ? new Coordinate() : sortUV;
    }

    public @NonNull Coordinate getSignInBtnUV() {
        return signInBtnUV = signInBtnUV == null ? new Coordinate() : signInBtnUV;
    }

    public @NonNull Coordinate getRewardOptionBtnUV() {
        return rewardOptionBtnUV = rewardOptionBtnUV == null ? new Coordinate() : rewardOptionBtnUV;
    }

    // endregion Always NON-NULL Getter

    /**
     * 获取默认配置
     *
     * @return original主题配置
     */
    @NonNull
    public static TexCoordinate getDefault() {
        return new TexCoordinate();
    }
}
