package com.example.myapplication.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

//campo de texto personalizado
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false, //marca el campo en rojo cuando la validacion falla
    supportingText: String? = null, //mensaje de ayuda o de error debajo del campo
    imeAction: ImeAction = ImeAction.Default, //boton de accion del teclado (ej: Buscar)
    keyboardActions: KeyboardActions = KeyboardActions.Default, //que hacer al presionar ese boton
    trailingContent: (@Composable () -> Unit)? = null //contenido opcional al final del campo (ej: boton de busqueda)
)
{
    var passwordVisible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value= value,
        onValueChange= onValueChange,
        label={Text(label)},
        leadingIcon={
            Icon(imageVector = leadingIcon, contentDescription = label)
        },
        trailingIcon = {
            if (trailingContent != null){
                trailingContent()
            } else if (isPassword){
                IconButton(onClick = {passwordVisible = !passwordVisible}) {
                    Icon(
                        imageVector = if(passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible) "Ocultar Clave" else "Mostrar Clave"
                    )
                }
            }
        },
        visualTransformation = if (isPassword && !passwordVisible){
            PasswordVisualTransformation()
        }else {
            VisualTransformation.None
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = keyboardActions,
        isError = isError,
        supportingText = supportingText?.let { mensaje -> { Text(mensaje) } },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    )
}
//opciones estandarizados y bordes redondeados
@Composable
fun CustomPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier= Modifier,
    enabled: Boolean= true
){
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ){
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium
        )
    }
}