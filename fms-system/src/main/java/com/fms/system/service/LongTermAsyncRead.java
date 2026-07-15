package com.fms.system.service;

import com.uhf.api.cls.GpiInfo_ST;
import com.uhf.api.cls.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * 读写器底层操作类（支持 USB 虚拟串口连接）
 * @author haoxin_chen
 */
public class LongTermAsyncRead {

    private volatile Reader rdr = null;
    private volatile boolean running = false;
    private final String comPort;
    private final int antennaCount;

    /**
     * 构造方法，传入串口地址和天线数量
     * @param comPort 串口名称，如 Windows: "COM4"，Linux: "/dev/ttyUSB0"
     * @param antennaCount 天线端口数（例如 1、4、8、16）
     */
    public LongTermAsyncRead(String comPort, int antennaCount) {
        this.comPort = comPort;
        this.antennaCount = antennaCount;
    }

    // ======================== 初始化与连接 ========================

    /**
     * 打开并初始化读写器（USB 虚拟串口模式）
     */
    public Reader.READER_ERR openReader() {
        Reader.READER_ERR err;
        if (rdr != null) {
            rdr.CloseReader();
            rdr = null;
        }
        rdr = new Reader();
        // 关键：使用串口地址（如 "COM4" 或 "/dev/ttyUSB0"）进行初始化
        err = rdr.InitReader_Notype(comPort, antennaCount);
        if (err != Reader.READER_ERR.MT_OK_ERR) {
            return err;
        }

        // 获取最大功率
        int[] maxpower = new int[1];
        err = rdr.ParamGet(Reader.Mtr_Param.MTR_PARAM_RF_MAXPOWER, maxpower);
        if (err != Reader.READER_ERR.MT_OK_ERR) {
            return err;
        }

        // 设置各天线功率（使用最大功率）
        Reader.AntPowerConf apcf = rdr.new AntPowerConf();
        apcf.antcnt = antennaCount;
        for (int i = 0; i < apcf.antcnt; i++) {
            Reader.AntPower jaap = rdr.new AntPower();
            jaap.antid = i + 1;
            jaap.readPower = (short) maxpower[0];
            jaap.writePower = (short) maxpower[0];
            apcf.Powers[i] = jaap;
        }
        err = rdr.ParamSet(Reader.Mtr_Param.MTR_PARAM_RF_ANTPOWER, apcf);
        if (err != Reader.READER_ERR.MT_OK_ERR) {
            return err;
        }

        // 设置 Gen2 协议
        Reader.Inv_Potls_ST ipst = rdr.new Inv_Potls_ST();
        ipst.potlcnt = 1;
        ipst.potls = new Reader.Inv_Potl[1];
        Reader.Inv_Potl ipl = rdr.new Inv_Potl();
        ipl.weight = 30;
        ipl.potl = Reader.SL_TagProtocol.SL_TAG_PROTOCOL_GEN2;
        ipst.potls[0] = ipl;
        err = rdr.ParamSet(Reader.Mtr_Param.MTR_PARAM_TAG_INVPOTL, ipst);
        if (err != Reader.READER_ERR.MT_OK_ERR) {
            return err;
        }

        // 可选：关闭天线检测（避免误报）
        rdr.ParamSet(Reader.Mtr_Param.MTR_PARAM_READER_IS_CHK_ANT, new int[]{0});

        return Reader.READER_ERR.MT_OK_ERR;
    }

    // ======================== 盘点控制 ========================

    /**
     * 启动快速模式盘点（使用天线1）
     */
    public void startReadTags() {
        int antcnt = 1;
        int[] ants = new int[antcnt];
        ants[0] = 1;
        Reader.READER_ERR readerErr = rdr.AsyncStartReading(ants, antcnt, 0);
        if (readerErr == Reader.READER_ERR.MT_OK_ERR) {
            running = true;
        }
    }

    /**
     * 停止快速模式盘点
     */
    public Reader.READER_ERR stopReadTask() {
        if (!running) {
            return Reader.READER_ERR.MT_OK_ERR;
        }
        Reader.READER_ERR er = rdr.AsyncStopReading();
        if (er == Reader.READER_ERR.MT_OK_ERR) {
            running = false;
        }
        return er;
    }

    // ======================== 读取标签 ========================

    /**
     * 读取当前缓冲区中所有标签的 EPC（以列表返回）
     */
    public List<String> readAllEpc() {
        List<String> epcList = new ArrayList<>();
        if (rdr == null) {
            return epcList;
        }

        // 如果正在运行，先停止再重启，以清空缓冲区
        if (running) {
            stopReadTask();          // 停止盘点
        }
        // 重新启动（天线1）
        startReadTags();            // 内部会设置 running = true
        // 等待一段时间让读写器捕获标签（根据实际环境调整，一般 200~500ms）
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 读取缓冲区中的标签
        int[] tagcnt = new int[1];
        Reader.READER_ERR er = rdr.AsyncGetTagCount(tagcnt);
        if (er == Reader.READER_ERR.MT_OK_ERR && tagcnt[0] > 0) {
            for (int i = 0; i < tagcnt[0]; i++) {
                Reader.TAGINFO tfs = rdr.new TAGINFO();
                er = rdr.AsyncGetNextTag(tfs);
                if (er == Reader.READER_ERR.MT_OK_ERR) {
                    epcList.add(Reader.bytes_Hexstr(tfs.EpcId));
                }
            }
        }
        return epcList;
    }

    // ======================== 写入 EPC ========================

    /**
     * 十六进制字符串转字节数组
     */
    private byte[] hexStringToBytes(String hex) {
        if (hex == null || hex.length() % 2 != 0) {
            return null;
        }
        byte[] data = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            int high = Character.digit(hex.charAt(i), 16);
            int low = Character.digit(hex.charAt(i + 1), 16);
            if (high == -1 || low == -1) return null;
            data[i / 2] = (byte) ((high << 4) + low);
        }
        return data;
    }

    /**
     * 将 int 密码转为 4 字节数组（大端）
     */
    private byte[] intToBytes(int value) {
        return new byte[]{
                (byte) ((value >> 24) & 0xFF),
                (byte) ((value >> 16) & 0xFF),
                (byte) ((value >> 8) & 0xFF),
                (byte) (value & 0xFF)
        };
    }

    /**
     * 写入指定 EPC（自动暂停/恢复盘点）
     * @param newEpc   新 EPC 十六进制字符串（长度必须为偶数）
     * @param password 访问密码（整数，默认 0）
     * @return true-成功
     */
    public boolean writeEpc(String newEpc, int password) {
        if (rdr == null) {
            System.err.println("读写器未初始化");
            return false;
        }

        boolean wasRunning = running;
        if (wasRunning) {
            Reader.READER_ERR stopErr = rdr.AsyncStopReading();
            if (stopErr != Reader.READER_ERR.MT_OK_ERR) {
                System.err.println("停止盘点失败，无法写入");
                return false;
            }
            running = false;
        }

        try {
            byte[] epcBytes = hexStringToBytes(newEpc);
            if (epcBytes == null || epcBytes.length == 0) {
                System.err.println("EPC格式错误：必须为偶数长度的十六进制字符串");
                return false;
            }

            byte[] passBytes = intToBytes(password);

            // 使用 WriteTagData 方法（兼容 SDK）
            Reader.READER_ERR err = rdr.WriteTagData(
                    1,                      // 天线号
                    (char) 0x01,            // EPC Bank
                    0x02,                   // 起始地址（word偏移）
                    epcBytes,               // 数据
                    epcBytes.length,        // 数据长度（字节数）
                    passBytes,              // 访问密码
                    (short) 1000            // 超时（毫秒）
            );

            if (err == Reader.READER_ERR.MT_OK_ERR) {
                System.out.println("EPC写入成功：" + newEpc);
                return true;
            } else {
                System.err.println("EPC写入失败，错误码：" + err);
                return false;
            }
        } finally {
            if (wasRunning) {

                int antcnt = 1;
                int[] ants = new int[]{1};
                Reader.READER_ERR restartErr = rdr.AsyncStartReading(ants, antcnt, 0);
                if (restartErr == Reader.READER_ERR.MT_OK_ERR) {
                    // 先停止再启动，清空缓冲区
                    rdr.AsyncStopReading();
                    rdr.AsyncStartReading(ants, 1, 0);
                    running = true;
                } else {
                    System.err.println("恢复盘点失败，请手动重新启动");
                }
            }
        }
    }

    public boolean writeEpc(String newEpc) {
        return writeEpc(newEpc, 0);
    }

    // ======================== 状态查询 ========================

    public boolean isRunning() {
        return running;
    }

    public Reader getReader() {
        return rdr;
    }
}
