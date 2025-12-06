// 加密工具函数

/**
 * 将字符串转换为ArrayBuffer
 * @param {string} str - 要转换的字符串
 * @returns {ArrayBuffer} - 转换后的ArrayBuffer
 */
const stringToArrayBuffer = (str) => {
  const encoder = new TextEncoder();
  return encoder.encode(str);
};

/**
 * 将ArrayBuffer转换为十六进制字符串
 * @param {ArrayBuffer} buffer - 要转换的ArrayBuffer
 * @returns {string} - 转换后的十六进制字符串
 */
const arrayBufferToHex = (buffer) => {
  return Array.from(new Uint8Array(buffer))
    .map(byte => byte.toString(16).padStart(2, '0'))
    .join('');
};

/**
 * 计算字符串的SHA256哈希值
 * @param {string} str - 要计算哈希值的字符串
 * @returns {Promise<string>} - SHA256哈希值的十六进制字符串
 */
export const sha256 = async (str) => {
  const buffer = stringToArrayBuffer(str);
  const hashBuffer = await crypto.subtle.digest('SHA-256', buffer);
  return arrayBufferToHex(hashBuffer);
};
