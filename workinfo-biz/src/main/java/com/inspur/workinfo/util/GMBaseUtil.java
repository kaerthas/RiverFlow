package com.inspur.workinfo.util;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.Security;

/**
 * <p>Copyright: Copyright (c) 2016 易海网络</p>
 *
 * @author Oskar
 * @version 1.0
 * @description
 * @date 2022-06-08
 */
public class GMBaseUtil {

    private static final String BASE_RESOURCE_PATH = "src/main/resources/";

    protected  GMBaseUtil() {
        throw new IllegalStateException("Utility class");
    }

    protected static String byte2Base64(byte[] bytes) {
        Base64 encoder = new Base64();
        return encoder.encodeAsString(bytes);
    }

    protected static byte[] base642Byte(String base64Key) {
        Base64 decoder = new Base64();
        return decoder.decode(base64Key);
    }

    protected static void writeFile(String filePath, byte[] data) throws Throwable {
        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }

        InputStream ins = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
        if (null == ins) {
            filePath = "src/main/resources/" + filePath;
            File file = new File(filePath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            boolean isCreate = file.createNewFile();
            if (!isCreate) {
                throw new IOException("create file failed,please check your privilege");
            } else {
                RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
                Throwable var6 = null;

                try {
                    raf.write(data);
                } catch (Throwable var15) {
                    var6 = var15;
                    throw var15;
                } finally {
                    if (raf != null) {
                        if (var6 != null) {
                            try {
                                raf.close();
                            } catch (Throwable var14) {
                                var6.addSuppressed(var14);
                            }
                        } else {
                            raf.close();
                        }
                    }

                }

            }
        } else {
            throw new IOException(filePath + " already exit,if you want to cover,please delete it first");
        }
    }

    protected static String loadKeyFromFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (file.exists()) {
            return loadKeyFromAbsoluteFile(filePath);
        } else {
            if (filePath.startsWith("/")) {
                filePath = filePath.substring(1);
            }

            try {
                InputStream ins = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
                Throwable var3 = null;

                try {
                    BufferedReader bReader = new BufferedReader(new InputStreamReader(ins, StandardCharsets.UTF_8));
                    Throwable var5 = null;

                    try {
                        StringBuilder sb = new StringBuilder();

                        String s;
                        while ((s = bReader.readLine()) != null) {
                            sb.append(s).append("\n");
                        }

                        String var8 = sb.toString();
                        return var8;
                    } catch (Throwable var33) {
                        var5 = var33;
                        throw var33;
                    } finally {
                        if (bReader != null) {
                            if (var5 != null) {
                                try {
                                    bReader.close();
                                } catch (Throwable var32) {
                                    var5.addSuppressed(var32);
                                }
                            } else {
                                bReader.close();
                            }
                        }

                    }
                } catch (Throwable var35) {
                    var3 = var35;
                    throw var35;
                } finally {
                    if (ins != null) {
                        if (var3 != null) {
                            try {
                                ins.close();
                            } catch (Throwable var31) {
                                var3.addSuppressed(var31);
                            }
                        } else {
                            ins.close();
                        }
                    }

                }
            } catch (IOException var37) {
                //if (log.isErrorEnabled()) {
                //    log.error("file exception,please make sure file exist at src/main/resources/{}, msg={},exception={}", new Object[]{filePath, var37.getMessage(), var37});
                //}

                return null;
            }
        }
    }

    protected static String loadKeyFromAbsoluteFile(String filePath) throws IOException {
        try {
            FileInputStream fin = new FileInputStream(filePath);
            Throwable var2 = null;

            try {
                InputStreamReader reader = new InputStreamReader(fin);
                Throwable var4 = null;

                try {
                    BufferedReader buffReader = new BufferedReader(reader);
                    Throwable var6 = null;

                    try {
                        StringBuilder sb = new StringBuilder();

                        String s;
                        while ((s = buffReader.readLine()) != null) {
                            sb.append(s).append("\n");
                        }

                        String var9 = sb.toString();
                        return var9;
                    } catch (Throwable var56) {
                        var6 = var56;
                        throw var56;
                    } finally {
                        if (buffReader != null) {
                            if (var6 != null) {
                                try {
                                    buffReader.close();
                                } catch (Throwable var55) {
                                    var6.addSuppressed(var55);
                                }
                            } else {
                                buffReader.close();
                            }
                        }

                    }
                } catch (Throwable var58) {
                    var4 = var58;
                    throw var58;
                } finally {
                    if (reader != null) {
                        if (var4 != null) {
                            try {
                                reader.close();
                            } catch (Throwable var54) {
                                var4.addSuppressed(var54);
                            }
                        } else {
                            reader.close();
                        }
                    }

                }
            } catch (Throwable var60) {
                var2 = var60;
                throw var60;
            } finally {
                if (fin != null) {
                    if (var2 != null) {
                        try {
                            fin.close();
                        } catch (Throwable var53) {
                            var2.addSuppressed(var53);
                        }
                    } else {
                        fin.close();
                    }
                }

            }
        } catch (IOException var62) {
            //if (log.isErrorEnabled()) {
            //    log.error("file exception,please make sure file exist at src/main/resources/{}, msg={},exception={}", new Object[]{filePath, var62.getMessage(), var62});
            //}

            return null;
        }
    }

    static {
        Security.addProvider(new BouncyCastleProvider());
    }
}
