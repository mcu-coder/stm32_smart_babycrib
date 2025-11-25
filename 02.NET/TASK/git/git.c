#include "git.h"

Data_TypeDef Data_init;						  // 设备数据结构体
Threshold_Value_TypeDef threshold_value_init; // 设备阈值设置结构体
Device_Satte_Typedef device_state_init;		  // 设备状态
DHT11_Data_TypeDef DHT11_Data;

extern U8 MG996R_TEST; // 变速测试

// 获取数据参数
mySta Read_Data(Data_TypeDef *Device_Data)
{
	Read_DHT11(&DHT11_Data); // 获取温湿度数据
	Device_Data->temperatuer = DHT11_Data.temp_int + DHT11_Data.temp_deci * 0.01;
	Device_Data->humiditr = DHT11_Data.humi_int + DHT11_Data.humi_deci * 0.01;
	Device_Data->light = Light_value();
	Device_Data->shake = Shake_value();

	return MY_SUCCESSFUL;
}
// 初始化
mySta Reset_Threshole_Value(Threshold_Value_TypeDef *Value, Device_Satte_Typedef *device_state)
{

	// 状态重置
	Value->MQ3_value = 200;
	Value->PH_value = 7;
	Value->CO2_value = 800;
	Data_init.music_s = 1;

	return MY_SUCCESSFUL;
}
// 更新OLED显示屏中内容
mySta Update_oled_massage()
{
#if OLED // 是否打开
	char str[50];
	if (Data_init.Page == 0)
	{
		if (Data_init.wning)
		{
			sprintf(str, "婴儿已醒来: %d ", device_state_init.waning_time);
		}
		else
		{
			sprintf(str, "婴儿睡眠中: %d ", device_state_init.waning_time);
		}
		OLED_ShowCH(0, 0, (unsigned char *)str);
		sprintf(str, "光照: %d        ", Data_init.light);
		OLED_ShowCH(0, 2, (unsigned char *)str);
		sprintf(str, "温度: %.1f      ", Data_init.temperatuer);
		OLED_ShowCH(0, 4, (unsigned char *)str);
		sprintf(str, "湿度: %.1f      ", Data_init.humiditr);
		OLED_ShowCH(0, 6, (unsigned char *)str);
	}
	else if (Data_init.Page == 1)
	{

		// 当前播放
		sprintf(str, "当前音乐: %d   ", Data_init.music_s);
		OLED_ShowCH(0, 2, (unsigned char *)str);
		// 播放和暂停
		if (device_state_init.music_state == 1 || device_state_init.music_state == 3)
		{
			sprintf(str, "正在播放   ");
		}
		else if (device_state_init.music_state == 0 || device_state_init.music_state == 2)
		{
			sprintf(str, "暂停中     ");
		}

		OLED_ShowCH(0, 4, (unsigned char *)str);
	}
#endif

	return MY_SUCCESSFUL;
}

// 更新设备状态
mySta Update_device_massage()
{
	// 震动
	if (Data_init.shake < 400)
	{
		device_state_init.shake_state = 1;
	}
	else
	{
		device_state_init.shake_state = 0;
	}

	// 震动和声音
	if (LEVEL2 == 0 && device_state_init.shake_state)
	{
		// 修改状态
		device_state_init.waning_state = 1;
		device_state_init.waning_time = 30;
	}
	// 开始之后，只要有一个满足调节满足都开始摇床
	if (device_state_init.waning_time > 1 && (LEVEL2 == 0 || device_state_init.shake_state))
	{
		// 修改状态
		device_state_init.waning_state = 1;
		device_state_init.waning_time = 30;
	}
	if (Data_init.Flage == 0)
	{
		// 补光
		if (Data_init.light < 100)
		{
			relay1out = 1;
		}
		else if (Data_init.light > 250)
		{
			relay1out = 0;
		}
	}
	else
	{
		relay1out = Data_init.Flage;
	}



	// 未进食
  if (LEVEL1 == 1)
	{
		// 醒来 / 远程控制
		if ( (device_state_init.waning_state == 1 && device_state_init.waning_time > 0)|| device_state_init.bed_state == 1)
		{
			MG996R_Speed();
		}
		else
		{
			// 进食中，停止摇床
			MG996R_TEST = 15;
			Automation_SG90_Angle(1, 15);
		}
	}
	else
	{
			// 进食中，停止摇床
			MG996R_TEST = 15;
			Automation_SG90_Angle(1, 15);
	}

	// 回传数据
	if (Data_init.App)
	{
		switch (Data_init.App)
		{
		case 1:
			SendMqtt(1); // 发送数据到APP
			break;
		case 2:
			SendMqtt(2); // 发送数据到APP
			break;
		}
		Data_init.App = 0;
	}

	return MY_SUCCESSFUL;
}

// 定时器
void Automation_Close(void)
{
	// 倒计时
	if (device_state_init.waning_time > 0)
	{
		
		device_state_init.waning_time--;
	}
	if (device_state_init.waning_time == 0)
	{
		device_state_init.waning_state = 0;
	}
	// 播放
	if (device_state_init.music_state == 1)
	{
		if (JR6001_BUSY_IO == 0)
		{
			if (Data_init.music_s < 4)
			{
				Data_init.music_s++;
			}
			else
			{
				Data_init.music_s = 1;
			}
		}
		// 循环播放
		JR6001_SongControl(Data_init.music_s, 1);
	}
	else
	{
		JR6001_Instruction((U8 *)Suspend, 0);
	}
}
// 检测按键是否按下
static U8 num_on = 0;
static U8 key_old = 0;
void Check_Key_ON_OFF()
{
	U8 key;
	key = KEY_Scan(1);
	// 与上一次的键值比较 如果不相等，表明有键值的变化，开始计时
	if (key != 0 && num_on == 0)
	{
		key_old = key;
		num_on = 1;
	}
	if (key != 0 && num_on >= 1 && num_on <= Key_Scan_Time) // 25*10ms
	{
		num_on++; // 时间记录器
	}
	if (key == 0 && num_on > 0 && num_on < Key_Scan_Time) // 短按
	{
		switch (key_old)
		{
		case KEY1_PRES:
			//			// 上一曲
			//			if (Data_init.Page == 1)
			//			{

			//				if (Data_init.music_s > 1)
			//				{
			//					Data_init.music_s--;
			//				}
			//				else
			//				{
			//					Data_init.music_s = 9;
			//				}

			//				JR6001_SongControl(Data_init.music_s, 0);
			//				device_state_init.music_state = 1;
			//			}
			// 下一曲
			if (Data_init.Page == 1)
			{
				if (Data_init.music_s < 4)
				{
					Data_init.music_s++;
				}
				else
				{
					Data_init.music_s = 1;
				}

				JR6001_SongControl(Data_init.music_s, 0);
				device_state_init.music_state = 1;
				JR6001_Instruction((U8 *)Play, 0);
			}
			break;
		case KEY2_PRES:

			// 暂停
			if (Data_init.Page == 1)
			{
				if (device_state_init.music_state == 1)
				{
					JR6001_Instruction((U8 *)Suspend, 0);
					device_state_init.music_state = 0;
				}
				else
				{
					JR6001_Instruction((U8 *)Play, 0);
					device_state_init.music_state = 1;
				}
			}
			break;
		case KEY3_PRES:

			break;

		default:
			break;
		}
		num_on = 0;
	}
	else if (key == 0 && num_on >= Key_Scan_Time) // 长按
	{
		switch (key_old)
		{
		case KEY1_PRES:
			OLED_Clear();
			if (Data_init.Page == 0)
			{
				Data_init.Page = 1;
			}
			else if (Data_init.Page == 1)
			{
				Data_init.Page = 0;
			}
			break;
		case KEY2_PRES:

			break;
		case KEY3_PRES:

			break;
		default:
			break;
		}
		num_on = 0;
	}
}
// 解析json数据
mySta massage_parse_json(char *message)
{

	cJSON *cjson_test = NULL; // 检测json格式
	cJSON *cjson_data = NULL; // 数据
	const char *massage;
	// 定义数据类型
	u8 cjson_cmd; // 指令,方向

	/* 解析整段JSO数据 */
	cjson_test = cJSON_Parse(message);
	if (cjson_test == NULL)
	{
		// 解析失败
		printf("parse fail.\n");
		return MY_FAIL;
	}

	/* 依次根据名称提取JSON数据（键值对） */
	cjson_cmd = cJSON_GetObjectItem(cjson_test, "cmd")->valueint;
	/* 解析嵌套json数据 */
	cjson_data = cJSON_GetObjectItem(cjson_test, "data");
	switch (cjson_cmd)
	{
	case 0x01: // 消息包
		Data_init.music_s = cJSON_GetObjectItem(cjson_data, "music_s")->valueint;
		JR6001_SongControl(Data_init.music_s, 0);
		Data_init.App = 1;
		break;
	case 0x02: // 消息包
		device_state_init.music_state = cJSON_GetObjectItem(cjson_data, "music")->valueint;
		if (device_state_init.music_state == 0)
		{
			JR6001_Instruction((U8 *)Suspend, 0);
		}
		else
		{
			JR6001_Instruction((U8 *)Play, 0);
		}
		Data_init.App = 1;
		break;
	case 0x03: // 数据包
		Data_init.Flage = cJSON_GetObjectItem(cjson_data, "led")->valueint;
		relay1out = Data_init.Flage;
		Data_init.App = 1;
		break;
	case 0x04: // 数据包
		device_state_init.bed_state  = cJSON_GetObjectItem(cjson_data, "bed")->valueint;
		Data_init.App = 1;
		break;
	
	default:
		break;
	}

	/* 清空JSON对象(整条链表)的所有数据 */
	cJSON_Delete(cjson_test);

	return MY_SUCCESSFUL;
}
