package com.petpal.swimmer_customer.ui.payment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import com.petpal.swimmer_customer.R
import com.petpal.swimmer_customer.databinding.FragmentPaymentBinding
import com.petpal.swimmer_customer.databinding.PaymentItemRowBinding
import com.petpal.swimmer_customer.ui.main.MainActivity
import com.petpal.swimmer_customer.ui.payment.repository.PaymentRepository
import com.petpal.swimmer_customer.ui.payment.vm.PaymentViewModel

class PaymentFragment : Fragment() {

    lateinit var fragmentPaymentBinding: FragmentPaymentBinding
    lateinit var mainActivity: MainActivity
    lateinit var paymentViewModel: PaymentViewModel



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // spinner array
        // getResources 활용 하려면 oncreate 이후에 실행 가능 기억하기
        val spinnerItems = resources.getStringArray(R.array.spinner_items)
        val spinnerList = arrayOf(spinnerItems[0], spinnerItems[1], spinnerItems[2], spinnerItems[3], spinnerItems[4])

        fragmentPaymentBinding = FragmentPaymentBinding.inflate(inflater)
        mainActivity = activity as MainActivity
        paymentViewModel = ViewModelProvider(mainActivity)[PaymentViewModel::class.java]

        paymentViewModel.run {
            itemList.observe(mainActivity) {
                fragmentPaymentBinding.paymentViewPager.adapter?.notifyDataSetChanged()
            }
            paymentFee.observe(mainActivity) {
                fragmentPaymentBinding.paymentConfirmButton.text = "${it}원 결제하기"
                fragmentPaymentBinding.paymentCheck.text = "총 상품 금액 : ${it}원"
            }
        }
        fragmentPaymentBinding.run {

            // 배송지 선택 button
            paymentDeliveryButton.setOnClickListener {
                // 배송지 선택 api 적용
            }

            // spinner
            paymentSpinner.run {
                val spinnerAdapter = ArrayAdapter<String>(
                    context,
                    android.R.layout.simple_spinner_item,
                    spinnerList
                )
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                adapter = spinnerAdapter

                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }
                }
            }

            // 상단 툴바
            toolbarPayment.run {
                setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
                val navigationIcon = navigationIcon
                navigationIcon?.setTint(ContextCompat.getColor(context, R.color.black))
                setNavigationOnClickListener {
                    // 백 버튼 문제 해결하기 -> 완료
                    Navigation.findNavController(fragmentPaymentBinding.root).popBackStack()
                        /*.navigate(R.id.itemDetailFragment)*/
                }
            }

            // 버튼
            paymentConfirmButton.run {
                setOnClickListener {
                    // 결제 완료 버튼
                    // seller한테 넘겨줄 order객체 서버로 전송하는 메서드 구현 예정

                    // complete -> 주문 완료 화면
                    // 주문 완료 화면으로 이동하기
                    Navigation.findNavController(fragmentPaymentBinding.root)
                        .navigate(R.id.action_paymentFragment_to_completeFragment)

                    // failed -> 서버 오류를 알려주는 dialog 생성


                }
            }

            // repos -> vm -> item 목록 받기
            paymentViewModel.getItemAndCalculatePrice()

            // 상품 정보 viewPager2
            paymentViewPager.apply {
                adapter = ItemRecyclerAdapter()
            }
            // indicater 구성 tabLayout
            TabLayoutMediator(paymentTab, paymentViewPager) {
                    tab, position -> paymentViewPager.setCurrentItem(tab.position)
            }.attach()

        }

        return fragmentPaymentBinding.root
    }

    // viewPager2에 붙여줄 recyclerAdapter
    inner class ItemRecyclerAdapter: RecyclerView.Adapter<ItemRecyclerAdapter.ItemViewHolder>() {
        inner class ItemViewHolder(paymentItemRowBinding: PaymentItemRowBinding): RecyclerView.ViewHolder(paymentItemRowBinding.root) {
            val paymentItemImage: ImageView
            val paymentItemName: TextView
            val paymentItemPrice: TextView
            val paymentItemQuantity: TextView
            val paymentItemColor: TextView
            val paymentItemSize: TextView

            init {
                paymentItemImage = paymentItemRowBinding.paymentItemImage
                paymentItemName = paymentItemRowBinding.paymentItemName
                paymentItemPrice = paymentItemRowBinding.paymentItemPrice
                paymentItemQuantity = paymentItemRowBinding.paymentItemQuantity
                paymentItemColor = paymentItemRowBinding.paymentItemColor
                paymentItemSize = paymentItemRowBinding.paymentItemSize
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val paymentItemRowBinding = PaymentItemRowBinding.inflate(layoutInflater)
            val itemViewHolder = ItemViewHolder(paymentItemRowBinding)
            paymentItemRowBinding.root.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            return itemViewHolder
        }

        override fun getItemCount(): Int {
            return paymentViewModel.itemList.value?.size!!
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            holder.paymentItemName.text = paymentViewModel.itemList.value?.get(position)?.name.toString()
            holder.paymentItemPrice.text = "가격 : ${paymentViewModel.itemList.value?.get(position)?.price.toString()}"

            // 현재 데이터셋 없으므로 1 고정
            holder.paymentItemQuantity.text = "수량 : 1"

            // option -> color, size로 분할
            holder.paymentItemColor.text = "색상 : ${paymentViewModel.itemList.value?.get(position)?.color}"
            holder.paymentItemSize.text = "사이즈 : ${paymentViewModel.itemList.value?.get(position)?.size}"
            PaymentRepository.getItemImage(holder.paymentItemImage, paymentViewModel.itemList.value?.get(position)?.mainImage)
        }
    }
}

