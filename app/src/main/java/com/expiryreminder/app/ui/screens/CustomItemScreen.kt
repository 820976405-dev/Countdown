package com.expiryreminder.app.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.expiryreminder.app.data.Item
import com.expiryreminder.app.ui.theme.AppColors
import com.expiryreminder.app.util.formatExpiryDate
import com.expiryreminder.app.viewmodel.CategoryViewModel
import com.expiryreminder.app.viewmodel.ItemViewModel
import kotlinx.coroutines.runBlocking
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomItemScreen(
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    itemViewModel: ItemViewModel,
    categoryViewModel: CategoryViewModel
) {
    val context = LocalContext.current
    
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("请选择分类") }
    var expireDate by remember { mutableStateOf(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000) }
    var imagePath by remember { mutableStateOf<String?>(null) }
    
    var remindBefore7Days by remember { mutableStateOf(false) }
    var remindBefore1Day by remember { mutableStateOf(false) }
    
    var note by remember { mutableStateOf("") }
    
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "自定义物品",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Button(
                onClick = {
                    if (name.isNotBlank() && selectedCategory != "请选择分类") {
                        val categoryId = runBlocking {
                            categoryViewModel.getCategoryByName(selectedCategory)?.id
                            ?: mainCategoriesList.flatMap { parent ->
                                listOf(parent.name to parent.id) +
                                    parent.subCategories.map { it.name to it.id }
                            }.find { it.first == selectedCategory }?.second
                            ?: 0L
                        }
                        val item = Item(
                            id = 0,
                            name = name,
                            categoryId = categoryId,
                            imageUri = imagePath,
                            expireDate = expireDate,
                            purchaseDate = null,
                            quantity = 1,
                            unit = "个",
                            location = "",
                            note = note,
                            remindDays = if (remindBefore7Days) "7" else (if (remindBefore1Day) "1" else ""),
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        itemViewModel.addItem(item)
                        onSaveSuccess()
                    } else {
                        Toast.makeText(context, "请填写完整信息", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.extended.successGreen)
            ) {
                Text("保存", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            ImageUploadSectionNew(imagePath = imagePath, onImageSelected = { 
                imagePath = it
            })

            Spacer(modifier = Modifier.height(24.dp))

            FormFieldWithCounter(
                label = "物品名称",
                placeholder = "请输入物品名称，如：XXX",
                value = name,
                onValueChange = { name = it },
                maxLength = 20
            )

            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                "${name.length}/20",
                fontSize = 11.sp,
                color = AppColors.extended.placeholderText,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            FormSection(title = "所属分类") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppColors.extended.inputBackground)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { }
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(selectedCategory, fontSize = 14.sp, color = if (selectedCategory == "请选择分类") AppColors.extended.placeholderText else AppColors.extended.textPrimary)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppColors.extended.chevronTint, modifier = Modifier.size(20.dp))
                    }
                }
                
                Text(
                    "单位：克、毫升、瓶等（选填）",
                    fontSize = 12.sp,
                    color = AppColors.extended.placeholderText,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "该物品是否具有周期性或有效期",
                fontSize = 13.sp,
                color = AppColors.extended.textTertiary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PeriodOptionCard(text = "有", isSelected = true, onClick = { }, modifier = Modifier.weight(1f))
                PeriodOptionCard(text = "无", isSelected = false, onClick = { }, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            FormSection(title = "到期日期") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, AppColors.extended.inputBorder, RoundedCornerShape(10.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { showDatePicker = true }
                        )
                        .padding(horizontal = 16.dp, vertical = 18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            formatExpiryDate(expireDate),
                            fontSize = 16.sp,
                            color = AppColors.extended.textPrimary
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = AppColors.extended.textTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = expireDate
                )
                
                androidx.compose.material3.DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                expireDate = millis
                                showDatePicker = false
                            }
                        }) {
                            Text("确定", color = AppColors.extended.successGreen, fontWeight = FontWeight.Medium)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("取消", color = AppColors.extended.textSecondary)
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            FormSection(title = "提醒时间") {
                ReminderToggleRow(
                    text = "到期前7天提醒",
                    isChecked = remindBefore7Days,
                    onCheckedChange = { remindBefore7Days = it }
                )
                
                Spacer(modifier = Modifier.height(6.dp))

                ReminderToggleRow(
                    text = "到期前1天提醒",
                    isChecked = remindBefore1Day,
                    onCheckedChange = { remindBefore1Day = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            FormSection(title = "备注（选填）") {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("可记录购买渠道、价格、批号等信息", color = AppColors.extended.placeholderText) },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.extended.successGreen,
                        unfocusedBorderColor = AppColors.extended.inputBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    "${note.length}/100",
                    fontSize = 11.sp,
                    color = AppColors.extended.placeholderText,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun FormFieldWithCounter(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    maxLength: Int
) {
    Column {
        Text(
            label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.extended.textPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        OutlinedTextField(
            value = value,
            onValueChange = { if (it.length <= maxLength) onValueChange(it) },
            placeholder = { Text(placeholder, color = AppColors.extended.placeholderText) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.extended.successGreen,
                unfocusedBorderColor = AppColors.extended.inputBorder
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun PeriodOptionCard(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) AppColors.extended.successGreenBg else AppColors.extended.inputBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) AppColors.extended.successGreen else AppColors.extended.textSecondary
        )
    }
}

@Composable
fun ReminderToggleRow(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!isChecked) }
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AppColors.extended.successGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = AppColors.extended.switchUncheckedTrack
            )
        )
        
        Spacer(modifier = Modifier.width(10.dp))
        
        Text(
            text,
            fontSize = 14.sp,
            color = AppColors.extended.textPrimary
        )
    }
}
