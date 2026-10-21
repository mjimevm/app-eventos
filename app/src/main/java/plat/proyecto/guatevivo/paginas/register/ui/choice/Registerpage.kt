package plat.proyecto.guatevivo.paginas.register.ui.choice

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import plat.proyecto.guatevivo.R
import plat.proyecto.guatevivo.core.bars.TopBar
import plat.proyecto.guatevivo.ui.theme.GuatevivoTheme

@Serializable
data object RegisterDestination

fun NavController.navigateToRegister(navOptions: NavOptions? = null) {
    navigate(RegisterDestination, navOptions)
}

fun NavGraphBuilder.registerScreen(
    onCreateNormalAccount: () -> Unit,
    onCreateBussinesAccount: () -> Unit,
    onBack: () -> Unit
) {
    composable<RegisterDestination> {
        RegisterRoute(
            onCreateNormalAccount = onCreateNormalAccount,
            onCreateBussinesAccount = onCreateBussinesAccount,
            onBack = onBack
        )
    }
}

@Composable
fun RegisterRoute(
    onCreateNormalAccount: () -> Unit,
    onCreateBussinesAccount: () -> Unit,
    onBack: () -> Unit
) {
    Register(onCreateNormalAccount = onCreateNormalAccount,
        onCreateBussinesAccount = onCreateBussinesAccount,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Register(modifier: Modifier = Modifier,
                     onBack: () -> Unit = {},
             onCreateNormalAccount: () -> Unit = {},
             onCreateBussinesAccount: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopBar(showBackButton = true, onBackClick = onBack)
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "¿Qué desea crear?",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 24.dp),
                    thickness = 3.dp,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Selecciona el tipo de cuenta que mejor se adapte a tus necesidades",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                SelectionCard(
                    title = "Cuenta de Usuario",
                    description = "Ideal para personas que desean descubrir y asistir a eventos increíbles.",
                    iconResId = R.drawable.account_circle,
                    onClick = onCreateNormalAccount
                )

                Spacer(modifier = Modifier.height(16.dp))

                SelectionCard(
                    title = "Cuenta de Compañía",
                    description = "Perfecta para empresas u organizadores que desean publicar y gestionar sus propios eventos.",
                    iconResId = R.drawable.company_icon,
                    onClick = onCreateBussinesAccount
                )
            }
        }
    }
}

@Composable
fun SelectionCard(
    title: String,
    description: String,
    iconResId: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = iconResId),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun RegisterPreview() {
    GuatevivoTheme {
        Register(onCreateNormalAccount = {}, onCreateBussinesAccount = {})
    }
}
