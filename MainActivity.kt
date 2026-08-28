package com.meucontrole.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

data class Tx(
    val id:String=UUID.randomUUID().toString(),
    val date:String,
    val type:String,
    val desc:String,
    val value:Double,
    val account:String,
    val essential:Boolean=false
)

class FinanceVM : ViewModel() {
    var balances by mutableStateOf(
        linkedMapOf("Levaê" to 141.50, "Giross" to 113.40, "Banco" to 137.79)
    )
        private set

    var tx by mutableStateOf(listOf(
        Tx(date="24/08/2026",type="Ganho",desc="Nuvem",value=26.00,account="Levaê"),
        Tx(date="24/08/2026",type="Ganho",desc="Giross",value=24.00,account="Giross"),
        Tx(date="24/08/2026",type="Gasto",desc="Farmacia",value=17.00,account="Levaê",essential=true),
        Tx(date="24/08/2026",type="Gasto",desc="Pilhas",value=8.00,account="Levaê"),
        Tx(date="24/08/2026",type="Gasto",desc="gasolina",value=15.00,account="Levaê",essential=true),
        Tx(date="25/08/2026",type="Ganho",desc="Giross",value=51.76,account="Giross"),
        Tx(date="25/08/2026",type="Ganho",desc="Nuvem",value=59.40,account="Levaê"),
        Tx(date="25/08/2026",type="Gasto",desc="Gasolina",value=15.00,account="Levaê",essential=true),
        Tx(date="26/08/2026",type="Ganho",desc="Nuvem",value=8.20,account="Levaê"),
        Tx(date="26/08/2026",type="Ganho",desc="Giross",value=34.44,account="Giross"),
        Tx(date="26/08/2026",type="Ganho",desc="Vendas",value=100.00,account="Levaê"),
        Tx(date="26/08/2026",type="Gasto",desc="Café",value=10.00,account="Levaê"),
        Tx(date="26/08/2026",type="Gasto",desc="Cigarros",value=15.00,account="Levaê"),
        Tx(date="26/08/2026",type="Gasto",desc="Gasolina",value=15.00,account="Levaê",essential=true),
        Tx(date="27/08/2026",type="Gasto",desc="Lazer",value=34.00,account="Levaê"),
        Tx(date="27/08/2026",type="Gasto",desc="Contas",value=100.00,account="Levaê",essential=true),
        Tx(date="27/08/2026",type="Gasto",desc="Aluguel",value=300.00,account="Levaê",essential=true)
    ))
    val weeklyGoal=1100.0
    val monthlyGainGoal=2800.0
    val netGoal=2800.0

    fun total() = balances.values.sum()
    fun add(t:Tx) {
        val m=balances.toMutableMap()
        m[t.account]=(m[t.account] ?: 0.0)+(if(t.type=="Ganho") t.value else -t.value)
        balances=m.toMap(LinkedHashMap())
        tx=tx+t
    }
    fun remove(t:Tx) {
        val m=balances.toMutableMap()
        m[t.account]=(m[t.account] ?: 0.0)+(if(t.type=="Ganho") -t.value else t.value)
        balances=m.toMap(LinkedHashMap())
        tx=tx.filterNot{it.id==t.id}
    }
    fun setBalance(account:String,value:Double) {
        val m=balances.toMutableMap(); m[account]=value
        balances=m.toMap(LinkedHashMap())
    }
}

fun brl(v:Double)=NumberFormat.getCurrencyInstance(Locale("pt","BR")).format(v)
fun percent(v:Double,d:Double)=if(d==0.0) "0,0%" else String.format(Locale("pt","BR"),"%.1f%%",v/d*100)

@Composable
fun BoxCard(content:@Composable ColumnScope.()->Unit)=Card(
    Modifier.fillMaxWidth().padding(vertical=5.dp)
){Column(Modifier.padding(16.dp),content=content)}

@Composable
fun Chip(selected:Boolean,onClick:()->Unit,text:String)=FilterChip(
    selected=selected,onClick=onClick,label={Text(text)},modifier=Modifier.padding(end=5.dp)
)

@Composable
fun App(vm:FinanceVM) {
    var tab by remember { mutableIntStateOf(0) }
    Scaffold(bottomBar={
        NavigationBar {
            val labels=listOf("Início","Lançar","Contas","Histórico","Ajustes")
            val icons=listOf(Icons.Default.Home,Icons.Default.Add,Icons.Default.AccountBalance,Icons.Default.List,Icons.Default.Settings)
            labels.forEachIndexed { i,label ->
                NavigationBarItem(selected=tab==i,onClick={tab=i},icon={Icon(icons[i],label)},label={Text(label)})
            }
        }
    }) { pad ->
        Column(Modifier.padding(pad).padding(horizontal=16.dp).fillMaxSize()) {
            Text("Meu Controle",style=MaterialTheme.typography.headlineMedium,modifier=Modifier.padding(vertical=14.dp))
            when(tab){0->Dashboard(vm);1->Launch(vm);2->Accounts(vm);3->History(vm);4->Settings(vm)}
        }
    }
}

@Composable
fun Dashboard(vm:FinanceVM) {
    val gains=vm.tx.sumOf{if(it.type=="Ganho")it.value else 0.0}
    val expenses=vm.tx.sumOf{if(it.type=="Gasto")it.value else 0.0}
    val essential=vm.tx.sumOf{if(it.type=="Gasto"&&it.essential)it.value else 0.0}
    LazyColumn {
        item { BoxCard {
            Text("SALDO DISPONÍVEL")
            Text(brl(vm.total()),style=MaterialTheme.typography.headlineLarge)
            Text("Meta líquida: ${brl(vm.netGoal)} • ${percent(vm.total(),vm.netGoal)}")
        }}
        item { BoxCard {
            Text("SEMANA 24/08 – 30/08",style=MaterialTheme.typography.titleMedium)
            Text("Ganhos: ${brl(gains)} / meta ${brl(vm.weeklyGoal)} • ${percent(gains,vm.weeklyGoal)}")
            Text("Gastos: ${brl(expenses)} • ${percent(expenses,gains)} dos ganhos")
            Text("Essenciais: ${brl(essential)} • ${percent(essential,expenses)} dos gastos")
        }}
        item { Text("Caixas",style=MaterialTheme.typography.titleLarge,modifier=Modifier.padding(top=10.dp)) }
        items(vm.balances.toList()) { (a,v) -> BoxCard {
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(a);Text(brl(v))}
        }}
    }
}

@Composable
fun Launch(vm:FinanceVM) {
    var desc by remember{mutableStateOf("")}
    var value by remember{mutableStateOf("")}
    var gain by remember{mutableStateOf(true)}
    var account by remember{mutableStateOf("Levaê")}
    var essential by remember{mutableStateOf(false)}
    Column {
        OutlinedTextField(desc,{desc=it},label={Text("Descrição")},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(value,{value=it},label={Text("Valor")},modifier=Modifier.fillMaxWidth())
        Row(Modifier.padding(vertical=5.dp)){Chip(gain,{gain=true},"Ganho");Chip(!gain,{gain=false},"Gasto")}
        Row(Modifier.padding(vertical=5.dp)){vm.balances.keys.forEach{a->Chip(account==a,{account=a},a)}}
        Row{Checkbox(essential,{essential=it});Text("Essencial",Modifier.padding(top=12.dp))}
        Button(onClick={
            value.replace(",",".").toDoubleOrNull()?.takeIf{it>0}?.let{
                vm.add(Tx(date="28/08/2026",type=if(gain)"Ganho" else "Gasto",
                    desc=desc.ifBlank{"Sem descrição"},value=it,account=account,essential=essential))
                desc="";value=""
            }
        },Modifier.fillMaxWidth()){Text("REGISTRAR")}
    }
}

@Composable
fun Accounts(vm:FinanceVM) {
    LazyColumn {
        item{Text("Saldos",style=MaterialTheme.typography.titleLarge)}
        items(vm.balances.toList()){(a,v)->
            var input by remember(v){mutableStateOf("%.2f".format(Locale.US,v))}
            BoxCard{
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                    Text(a,Modifier.weight(1f))
                    OutlinedTextField(input,{input=it},singleLine=true,modifier=Modifier.width(140.dp))
                    Button(onClick={input.replace(",",".").toDoubleOrNull()?.let{vm.setBalance(a,it)}}){Text("Salvar")}
                }
            }
        }
        item{
            Text("Contas pendentes",style=MaterialTheme.typography.titleLarge,modifier=Modifier.padding(top=10.dp))
            BoxCard{Text("Nenhuma conta pendente nesta beta. O módulo completo será persistente na próxima versão.")}
        }
    }
}

@Composable
fun History(vm:FinanceVM) {
    LazyColumn {
        items(vm.tx.reversed()){t->
            BoxCard{
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
                    Column(Modifier.weight(1f)){Text(t.desc);Text("${t.date} • ${t.account}${if(t.essential)" • essencial" else ""}",style=MaterialTheme.typography.bodySmall)}
                    Column{Text((if(t.type=="Ganho")"+" else "−")+brl(t.value));TextButton(onClick={vm.remove(t)}){Text("Excluir")}}
                }
            }
        }
    }
}

@Composable
fun Settings(vm:FinanceVM) {
    Column {
        BoxCard{Text("METAS",style=MaterialTheme.typography.titleMedium);Text("Ganhos semanais: ${brl(vm.weeklyGoal)}");Text("Ganhos mensais brutos: ${brl(vm.monthlyGainGoal)}");Text("Saldo líquido: ${brl(vm.netGoal)}")}
        BoxCard{Text("BETA 0.1");Text("Projeto nativo Kotlin + Jetpack Compose. Dados desta beta ficam em memória.")}
    }
}

class MainActivity:ComponentActivity(){
    override fun onCreate(state:Bundle?){super.onCreate(state);setContent{MaterialTheme{App(viewModel())}}}
}
