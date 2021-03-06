```wiki
IP地址是一个32位的二进制数，由四个八位字段组成。每个IP地址包括两部分：一部分为网络标识，一部分为主机标识。

A类地址前8位为网络标识。后24位为主机标识。网段与主机数的计算方法如下：
A类网段计算：
根据规定，A类地址的网络标识必须以“0”开头。那么其网段数应该为0XXXXXXX．YYYYYYYY．YYYYYYYY．YYYYYYYY即后面有七位数字，因为是二进制数，所以网段数应该是2的7次幂个网段，等于128，即网段应该是0—127之间。而网络空间计算都必须“减2”，这是因为要扣除两个保留地址：二进制数里全是“0”和全是“1”的要保留。“0”做为网络号，“1”做为广播号。所以A类地址的网段为1—126.
A类主机数计算：
因为后面24位是主机标识，所以主机数应该是224，即2的24次幂
2^24=4^12=16^6=256^3=16777216，扣除两个保留地址后，主机最大数应该是16777214个。
综上所述，A类IP地址范围应该是：1.0.0.1~126.255.255.254

B类网段计算：
根据规定，A类地址的网络标识必须以“10”开头。那么其网段数应该为10XXXXXX．XXXXXXXX．YYYYYYYY．YYYYYYYY即后面有14位数字，因为是二进制数，所以网段数应该为：
2^14，即2的14次幂个网段，等于16384，扣除两个全“0”，全“1”的保留地址，所以B类网络可以有16382个网段。而转换成十进制后,IP地址的第一个小数点前的数字应该是多少呢？因为第一段是10XXXXXX，所以应该是26个，即2的6次幂，等于64个。127是被保留网段暂不使用。所以网段应该是从128开始，到128+64-1=191.即十进制IP的第一段数字是在128—191之间。
B类主机数计算：
因为后面16位是主机标识，所以主机数应该是2^16，即2的16次幂
2^16=4^8=16^4=256^2=65536，扣除两个保留地址后，主机最大数应该是65534个。
综上所述，B类IP地址范围应该是：128.0.0.1~191.255.255.254

C类网段计算：
根据规定，C类地址的网络标识必须以“110”开头。那么其网段数应该为110XXXXX．XXXXXXXX．XXXXXXXX．YYYYYYYY即后面有21位数字，因为是二进制数，所以网段数应该为：2^21，即2的21次幂个网段，等于2097152，扣除两个全“0”，全“1”的保留地址，所以B类网络可以有2097150个网段。
而转换成十进制后，IP地址的第一个小数点前的数字应该是多少呢？因为第一段是110XXXXX，所以应该是25个，即2的5次幂，等于32个。所以网段应该是从192开始，到192+32-1=223.即十进制IP的第一段数字是在192—223之间。
C类主机数计算：
因为后面8位是主机标识，所以主机数应该是28，即2的8次幂
2^8=4^4=16^2=256^2，扣除两个保留地址后，主机最大数应该是254个。
综上所述，C类IP地址范围应该是：192.0.0.1~223.255.255.254
```